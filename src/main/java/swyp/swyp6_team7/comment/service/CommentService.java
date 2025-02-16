package swyp.swyp6_team7.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.request.CommentUpdateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.likes.dto.response.LikeReadResponseDto;
import swyp.swyp6_team7.likes.repository.LikeRepository;
import swyp.swyp6_team7.likes.util.LikeStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.notification.service.CommentNotificationService;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final TravelRepository travelRepository;
    private final CommunityRepository communityRepository;
    private final ImageRepository imageRepository;
    private final CommentNotificationService commentNotificationService;

    // Create
    @Transactional
    public Comment create(CommentCreateRequestDto request, int userNumber, String relatedType, int relatedNumber) {
        // 게시물 존재 여부 검증
        validateRelatedPostExists(relatedType, relatedNumber);

        // parentNumber가 0이 아닐 경우 해당 댓글이 존재하는지 검증
        if (request.getParentNumber() != 0) {
            validateCommentExists(request.getParentNumber());
        }

        Comment savedComment = commentRepository.save(request.toCommentEntity(
                userNumber,
                request.getContent(),
                request.getParentNumber(),
                LocalDateTime.now(), // regDate
                relatedType,
                relatedNumber
        ));
        log.info("댓글 생성 성공 - 댓글 번호: {}, 사용자 번호: {}, 관련 타입: {}, 관련 번호: {}",
                savedComment.getCommentNumber(), userNumber, relatedType, relatedNumber);

        // 댓글 알림 전송
        if (relatedType.equals("travel")) {
            commentNotificationService.createTravelCommentNotification(userNumber, relatedNumber);
        } else if (relatedType.equals("community")) {
            commentNotificationService.createCommunityCommentNotification(userNumber, relatedNumber);
        }

        return savedComment;
    }

    //댓글 번호로 댓글 조회(response 용)
    public CommentDetailResponseDto getCommentByNumber(int commentNumber) {
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. 댓글 번호: " + commentNumber));

        long likes = likeRepository.countByRelatedTypeAndRelatedNumber("comment", commentNumber);

        log.info("댓글 상세 조회 성공 - 댓글 번호: {}, 좋아요 수: {}", commentNumber, likes);
        return new CommentDetailResponseDto(comment, likes);
    }

    private void validateRelatedPostExists(String relatedType, int relatedNumber) {
        boolean exists;
        if ("travel".equals(relatedType)) {
            exists = travelRepository.existsTravelByNumber(relatedNumber);
        } else if ("community".equals(relatedType)) {
            exists = communityRepository.existsByPostNumber(relatedNumber);
        } else {
            throw new IllegalArgumentException("유효하지 않은 게시물 종류입니다. 게시물 타입: " + relatedType);
        }

        if (!exists) {
            throw new IllegalArgumentException("존재하지 않는 게시물입니다. 게시물 타입: " + relatedType + ", 게시물 번호: " + relatedNumber);
        }
    }

    private void validateCommentExists(int commentNumber) {
        if (!commentRepository.existsByCommentNumber(commentNumber)) {
            throw new IllegalArgumentException("부모 댓글이 존재하지 않습니다. 댓글 번호: " + commentNumber);
        }
    }

    //댓글 목록 조회
    @Transactional
    public List<CommentListReponseDto> getList(String relatedType, int relatedNumber, Integer userNumber) {
        //이때 userNumber는 댓글 조회 요청자

        validateRelatedPostExists(relatedType, relatedNumber);

        //List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
        List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
        if (comments.isEmpty()) {
            log.info("댓글이 없습니다: relatedType={}, relatedNumber={}", relatedType, relatedNumber);
            return Collections.emptyList();
        }
        log.info("댓글 목록 조회: 댓글 개수={}", comments.size());
        List<Comment> sortedComments = sortComments(comments);

        return convertToResponseDtos(sortedComments, userNumber);
    }

    //댓글 목록 조회 (페이징)
    @Transactional
    public Page<CommentListReponseDto> getListPage(PageRequest pageRequest, String relatedType, int relatedNumber, Integer userNumber) {

        validateRelatedPostExists(relatedType, relatedNumber);

        //List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
        List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
        if (comments.isEmpty()) {
            log.info("댓글이 없습니다: relatedType={}, relatedNumber={}", relatedType, relatedNumber);
            return Page.empty();
        }
        log.info("댓글 목록 조회: 댓글 개수={}", comments.size());
        List<Comment> sortedComments = sortComments(comments);

        List<CommentListReponseDto> responseDtos = convertToResponseDtos(sortedComments, userNumber);
        return toPage(responseDtos, pageRequest);
    }

    // update
    @Transactional
    public CommentDetailResponseDto update(CommentUpdateRequestDto request, int userNumber, int commentNumber) {
        // 댓글 존재 여부 검증 검증
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. 댓글 번호: " + commentNumber));

        // 댓글 작성자인지 확인
        validateCommentWriter(commentNumber, userNumber);

        // 업데이트 동작
        comment.update(request.getContent());
        commentRepository.save(comment);

        // 업데이트된 댓글의 detail 리턴
        long likes = likeRepository.countByRelatedTypeAndRelatedNumber("comment", commentNumber);
        log.info("댓글 수정 성공 - 댓글 번호: {}, 사용자 번호: {}", commentNumber, userNumber);

        return new CommentDetailResponseDto(comment, likes);
    }

    //Delete
    @Transactional
    public void delete(int commentNumber, int userNumber) {
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. 댓글 번호: " + commentNumber));

        validateCommentWriterOrPostWriter(commentNumber, userNumber);

        try {
            //댓글 삭제
            deleteReplies(comment);
            likeRepository.deleteByRelatedTypeAndRelatedNumber("comment", commentNumber);
            commentRepository.deleteByCommentNumber(commentNumber);

            log.info("댓글 삭제 성공 - 댓글 번호: {}, 사용자 번호: {}", commentNumber, userNumber);
        } catch (Exception e) {
            log.error("댓글 삭제 중 오류 발생 - 댓글 번호: {}, 오류: {}", commentNumber, e.getMessage());
            throw new RuntimeException("댓글 삭제 실패: " + e.getMessage());
        }
    }

    private void deleteReplies(Comment comment) {
        List<Comment> replies = commentRepository.findByRelatedTypeAndRelatedNumberAndParentNumber(
                comment.getRelatedType(), comment.getRelatedNumber(), comment.getCommentNumber());

        for (Comment reply : replies) {
            try {
                commentRepository.deleteByCommentNumber(reply.getCommentNumber());
                likeRepository.deleteByRelatedTypeAndRelatedNumber("comment", reply.getCommentNumber());
            } catch (Exception e) {
                log.error("답글 삭제 중 오류 발생 - 댓글 번호: {}, 오류: {}", reply.getCommentNumber(), e.getMessage());
            }
        }
    }

    private List<CommentListReponseDto> convertToResponseDtos(List<Comment> comments, Integer userNumber) {
        List<CommentListReponseDto> responseDtos = new ArrayList<>();
        for (Comment comment : comments) {
            Optional<Users> user = userRepository.findByUserNumber(comment.getUserNumber());
            String commentWriter = user.map(Users::getUserName).orElse("unknown");

            String imageUrl = imageRepository.findByRelatedTypeAndRelatedNumber("profile", comment.getUserNumber())
                    .map(img -> img.getUrl()).orElse(null);

            long repliesCount = comment.getParentNumber() == 0 ?
                    commentRepository.countByRelatedTypeAndRelatedNumberAndParentNumber(
                            comment.getRelatedType(), comment.getRelatedNumber(), comment.getCommentNumber()) : 0;

            LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(likeRepository, "comment", comment.getCommentNumber(), userNumber);

            Boolean commented = getCommented(comment, userNumber);

            int postWriterNumber = getPostWriterNumber(comment);

            responseDtos.add(CommentListReponseDto.fromEntity(comment, commentWriter, repliesCount,
                    likeStatus.getTotalLikes(), likeStatus.isLiked(), postWriterNumber, imageUrl, commented));
        }
        return responseDtos;
    }

    //댓글 작성자인지 확인
    @Transactional(readOnly = true)
    public void validateCommentWriter(int commentNumber, int userNumber) {

        // 존재하는 댓글인지 확인
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. 댓글 번호: " + commentNumber));

        // 요청한 사용자(=로그인 중인 사용자)가 댓글 작성자인지 확인
        if (comment.getUserNumber() != userNumber) {
            throw new IllegalArgumentException("댓글 작성자가 아닙니다.");
        }
    }

    // 댓글 작성자 혹은 게시글 작성자인지 검증하는 메소드
    @Transactional(readOnly = true)
    public void validateCommentWriterOrPostWriter(int commentNumber, int userNumber) {

        //존재하는 댓글인지 확인
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. 댓글 번호: " + commentNumber));

        int postWriterNumber = getPostWriterNumber(comment);
        ;
        if (comment.getRelatedType().equals("travel")) {
            // 댓글 번호로 게시글 번호 가져오기
            int travelNumber = comment.getRelatedNumber();
            // 게시글 번호로 작성자 회원 번호 가져오기
            postWriterNumber = travelRepository.findByNumber(travelNumber).get().getUserNumber();
        } else if (comment.getRelatedType().equals("community")) {
            int communityNumber = comment.getRelatedNumber();
            postWriterNumber = communityRepository.findByPostNumber(communityNumber).get().getUserNumber();
        }

        // 요청한 사용자(=로그인 중인 사용자)가 댓글 작성자인지 확인
        int commentWriter = comment.getUserNumber();

        // 요청한 사용자(=로그인 중인 사용자)가 댓글 작성자 혹은 게시글 작성자인지 확인
        if (userNumber != postWriterNumber && commentWriter != userNumber) {
            throw new IllegalArgumentException("댓글 작성자 혹은 게시글 작성자에게만 유효한 동작입니다.");
        }
    }

    private int getPostWriterNumber(Comment comment) {
        if ("travel".equals(comment.getRelatedType())) {
            return travelRepository.findByNumber(comment.getRelatedNumber())
                    .map(Travel::getUserNumber).orElse(0);
        } else if ("community".equals(comment.getRelatedType())) {
            return communityRepository.findByPostNumber(comment.getRelatedNumber())
                    .map(Community::getUserNumber).orElse(0);
        } else {
            throw new IllegalArgumentException("유효하지 않은 게시물 종류입니다. 관련 타입: " + comment.getRelatedType());
        }
    }


    // 댓글을 정렬하는 메소드
    private List<Comment> sortComments(List<Comment> allComments) {
        List<Comment> sortedComments = new ArrayList<>();
        Map<Integer, List<Comment>> parentToChildrenMap = new HashMap<>();

        // 부모 댓글과 자식 댓글을 분리
        for (Comment comment : allComments) {
            if (comment.getParentNumber() == 0) { // 부모 댓글인 경우
                sortedComments.add(comment);
            } else { // 자식 댓글인 경우
                parentToChildrenMap
                        .computeIfAbsent(comment.getParentNumber(), k -> new ArrayList<>())
                        .add(comment);
            }
        }

        // 부모 댓글 아래에 자식 댓글 추가
        List<Comment> finalSortedComments = new ArrayList<>();
        for (Comment parent : sortedComments) {
            finalSortedComments.add(parent); // 부모 댓글 추가
            List<Comment> children = parentToChildrenMap.get(parent.getCommentNumber());
            if (children != null) {
                finalSortedComments.addAll(children); // 자식 댓글 추가
            }
        }
        return finalSortedComments;
    }

    //Page 객체 생성
    private Page<CommentListReponseDto> toPage(List<CommentListReponseDto> responses, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());

        if (start > end) {
            start = end;
        }
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    //답글 작성 여부 가져오기
    private Boolean getCommented(Comment comment, Integer userNumber) {

        if (userNumber == null) {
            return false;
        }

        //나의 자식 댓글들 조회
        List<Comment> childComments = commentRepository.findByRelatedTypeAndRelatedNumberAndParentNumber(
                comment.getRelatedType(), comment.getRelatedNumber(), comment.getCommentNumber());

        //자식 답글들을 순회하며 조회 중인 유저 번호와 일치하는지 확인
        for (Comment childComment : childComments) {
            if (childComment.getUserNumber() == userNumber) {
                //하나라도 일치할 경우 commented를 true하고 for문 빠져나오기
                return true;
            }
        }

        return false;
    }
}