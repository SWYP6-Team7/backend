package swyp.swyp6_team7.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.request.CommentUpdateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.likes.dto.response.CommentLikeReadResponseDto;
import swyp.swyp6_team7.likes.repository.CommentLikeRepository;
import swyp.swyp6_team7.likes.service.CommentLikeService;
import swyp.swyp6_team7.likes.util.CommentLikeStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.repository.TravelRepository;
import swyp.swyp6_team7.travel.service.TravelService;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    final TravelService travelService;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final TravelRepository travelRepository;

    // Create
    @Transactional
    public Comment create(CommentCreateRequestDto request, int userNumber, String relatedType, int relatedNumber) {
        // 게시물 존재 여부 검증
        ResponseEntity<String> validationResponse = validateRelatedNumber(relatedType, relatedNumber);
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException(validationResponse.getBody());
        }

        // parentNumber가 0이 아닐 경우 해당 댓글이 존재하는지 검증
        if (request.getParentNumber() != 0) {
            Optional<Comment> parentComment = commentRepository.findByCommentNumber(request.getParentNumber());
            if (parentComment.isEmpty()) {
                throw new IllegalArgumentException("부모 댓글이 존재하지 않습니다: " + request.getParentNumber());
            }
        }

        Comment savedComment = commentRepository.save(request.toCommentEntity(
                userNumber,
                request.getContent(),
                request.getParentNumber(),
                LocalDateTime.now(), // regDate
                relatedType,
                relatedNumber
        ));
        return savedComment;
    }

    //댓글 번호로 댓글 조회(response 용)
    public CommentDetailResponseDto getCommentByNumber(int commentNumber) {
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다." + commentNumber));
        long likes = commentLikeRepository.countByCommentNumber(commentNumber);
        CommentDetailResponseDto detailResponse = new CommentDetailResponseDto(comment, likes);
        return detailResponse;
    }


    //해당 게시글이 존재하는지 검증하는 메소드
    @Transactional
    public ResponseEntity<String> validateRelatedNumber(String relatedType, int relatedNumber) {
        // 여행 게시글일 경우
        if (relatedType.equals("travel")) {
            try {
                TravelDetailResponse travelDetailResponse = travelService.getDetailsByNumber(relatedNumber);
                return ResponseEntity.ok("게시물 존재 유무 검증 성공.");
            } catch (IllegalArgumentException e) {
                // 검증 실패
                return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다." + e.getMessage());
            }
            //커뮤니티 게시글일 경우
//        } else if (relatedType.equals("community")) {
//
        } else {
            return ResponseEntity.badRequest().body("유효하지 않은 게시물 종류 입니다.");
        }
    }


    @Transactional
    public List<CommentListReponseDto> getListByrelatedNumber(String relatedType, int relatedNumber, int userNumber) {

        if (relatedType.equals("travel")) {
            List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
            List<Comment> sortedComments = sortComments(comments);


            List<CommentListReponseDto> listReponse = new ArrayList<>();
            for (Comment comment : sortedComments) {

                //작성자 조회
                Optional<Users> user = userRepository.findByUserNumber(comment.getUserNumber());
                String writer = user.map(Users::getUserName).orElse("unknown");

                //답글 수 계산
                long repliesCount = commentRepository.countByRelatedTypeAndRelatedNumberAndParentNumber(relatedType, relatedNumber, comment.getParentNumber());

                //좋아요 상태 가져오기
                CommentLikeReadResponseDto likeStatus = CommentLikeStatus.getCommentLikeStatus(commentLikeRepository, comment.getCommentNumber(), userNumber);
                //좋아요 개수
                long likes = likeStatus.getLikes();
                //좋아요 여부, true = 좋아요 누름
                boolean liked = likeStatus.isLiked();

                //게시글 작성자 회원번호
                int travelWriterNumber = travelRepository.findByNumber(relatedNumber).get().getUserNumber();

                //DTO
                CommentListReponseDto dto = CommentListReponseDto.fromEntity(comment, writer, repliesCount, likes, liked, travelWriterNumber);
                listReponse.add(dto);
            }
            return listReponse;
        } else {
            throw new IllegalArgumentException("유효하지 않은 게시물 종류입니다: " + relatedType);
        }
    }

    // update
    @Transactional
    public CommentDetailResponseDto update(CommentUpdateRequestDto request, int commentNumber, int userNumber) {
        // 댓글 존재 여부 검증 검증
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다: " + commentNumber));

        // 댓글 작성자 혹은 게시글 작성자인지 확인
        validateCommentWriter(commentNumber, userNumber);

        // 업데이트 동작
        comment.update(request.getContent()); // 수정할 내용을 설정

        // 업데이트 저장
        commentRepository.save(comment);

        // 업데이트된 댓글의 detail 리턴
        long likes = commentLikeRepository.countByCommentNumber(commentNumber);
        CommentDetailResponseDto result = new CommentDetailResponseDto(comment, likes);

        return result;
    }

    //Delete
    @Transactional
    public void delete(int commentNumber, int userNumber) {
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("comment not found: " + commentNumber));

        validateCommentWriter(commentNumber, userNumber);

        try {
            // 답글 삭제
            List<Comment> replies = commentRepository.findByRelatedTypeAndRelatedNumberAndParentNumber(comment.getRelatedType(), comment.getRelatedNumber(), comment.getCommentNumber());
            for (Comment reply : replies) {
                try {
                    commentRepository.deleteByCommentNumber(reply.getCommentNumber());
                    commentLikeRepository.deleteByCommentNumber(reply.getCommentNumber());

                } catch (Exception e) {
                    log.error("Failed to delete reply comment: {}", reply.getCommentNumber(), e);
                    // 추가적인 예외 처리 로직이 필요할 경우 여기에 구현
                }
            }

            // 좋아요 기록 삭제
            commentLikeRepository.deleteByCommentNumber(commentNumber);
            // 부모 댓글 삭제
            commentRepository.deleteByCommentNumber(commentNumber);
        } catch (Exception e) {
            log.error("댓글 삭제 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("댓글 삭제 실패: " + e.getMessage());
        }
    }


    // 댓글 작성자 혹은 게시글 작성자인지 검증하는 메소드
    @Transactional(readOnly = true)
    public void validateCommentWriter(int commentNumber, int userNumber) {

        //존재하는 댓글인지 확인
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("comment not found: " + commentNumber));

        // 댓글 번호로 게시글 번호 가져오기
        int travelNumber = comment.getRelatedNumber();

        // 게시글 번호로 작성자 회원 번호 가져오기
        int travelWriterNumber = travelRepository.findByNumber(travelNumber).get().getUserNumber();


        // 요청한 사용자(=로그인 중인 사용자)가 댓글 작성자 혹은 게시글 작성자인지 확인
        if (comment.getUserNumber() != userNumber | travelWriterNumber != userNumber) {
            throw new IllegalArgumentException("댓글 작성자 혹은 게시글 작성자에게만 유효한 동작입니다.");
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
}