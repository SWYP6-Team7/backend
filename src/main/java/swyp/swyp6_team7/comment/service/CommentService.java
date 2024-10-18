package swyp.swyp6_team7.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import swyp.swyp6_team7.notification.service.NotificationService;
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
    private final NotificationService notificationService;

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

        //create notification to Host and Enrolled Users
        notificationService.createCommentNotifications(userNumber, relatedType, relatedNumber);

        return savedComment;
    }

    //댓글 번호로 댓글 조회(response 용)
    public CommentDetailResponseDto getCommentByNumber(int commentNumber) {
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다." + commentNumber));
        long likes = likeRepository.countByRelatedTypeAndRelatedNumber("comment", commentNumber);
        CommentDetailResponseDto detailResponse = new CommentDetailResponseDto(comment, likes);
        return detailResponse;
    }


    //해당 게시글이 존재하는지 검증하는 메소드
    @Transactional
    public ResponseEntity<String> validateRelatedNumber(String relatedType, int relatedNumber) {
        // 여행 게시글일 경우
        if (relatedType.equals("travel")) {
            try {
                travelRepository.findByNumber(relatedNumber);
                return ResponseEntity.ok("게시물 존재 유무 검증 성공.");
            } catch (IllegalArgumentException e) {
                // 검증 실패
                return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다." + e.getMessage());
            }
            //커뮤니티 게시글일 경우
        } else if (relatedType.equals("community")) {
            try {
                Optional<Community> community = communityRepository.findByPostNumber(relatedNumber);
                return ResponseEntity.ok("게시물 존재 유무 검증 성공.");
            } catch (IllegalArgumentException e) {
                // 검증 실패
                return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다." + e.getMessage());
            }

        } else {
            return ResponseEntity.badRequest().body("유효하지 않은 게시물 종류 입니다.");
        }
    }

    //댓글 목록 조회
    @Transactional
    public List<CommentListReponseDto> getList(String relatedType, int relatedNumber, int userNumber) {
        //이때 userNumber는 댓글 조회 요청자

        if (relatedType.equals("travel")) {
            travelRepository.findByNumber(relatedNumber)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다." + relatedType + " : " + relatedNumber));

        } else if (relatedType.equals("community")) {
            communityRepository.findByPostNumber(relatedNumber)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다." + relatedType + " : " + relatedNumber));
        } else {
            throw new IllegalArgumentException("잘못된 타입입니다. : " + relatedType);
        }

        if (relatedType.equals("travel")) {
            List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
            List<Comment> sortedComments = sortComments(comments);


            List<CommentListReponseDto> listReponse = new ArrayList<>();
            for (Comment comment : sortedComments) {

                //댓글 작성자 조회
                Optional<Users> user = userRepository.findByUserNumber(comment.getUserNumber());
                String commentWriter = user.map(Users::getUserName).orElse("unknown");

                // 댓글 작성자 프로필 이미지 URL
                String imageUrl = imageRepository.findByRelatedTypeAndRelatedNumber("profile", comment.getUserNumber()).get().getUrl();


                // 답글 수 계산: 부모 댓글일 때만 계산
                long repliesCount = 0;
                if (comment.getParentNumber() == 0) {// 부모일 경우
                    repliesCount = commentRepository.countByRelatedTypeAndRelatedNumberAndParentNumber(relatedType, relatedNumber, comment.getCommentNumber()); // 답글 계산
                } else {
                    repliesCount = 0; //답글일 경우 답글 개수 0개
                }
                //좋아요 상태 가져오기
                LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(likeRepository, "comment", comment.getCommentNumber(), userNumber);
                //좋아요 개수
                long likes = likeStatus.getTotalLikes();
                //좋아요 여부, true = 좋아요 누름
                boolean liked = likeStatus.isLiked();

                //게시글 작성자 회원번보
                //게시글 정보 가져오기
                Optional<Travel> travelInfo = travelRepository.findByNumber(relatedNumber);
                int travelWriterNumber = travelInfo.get().getUserNumber();

                //답글 작성 여부 가져오기
                Boolean commented = getCommented(comment, userNumber);

                //DTO
                CommentListReponseDto dto = CommentListReponseDto.fromEntity(comment, commentWriter, repliesCount, likes, liked, travelWriterNumber, imageUrl,commented);
                listReponse.add(dto);
            }
            return listReponse;


        } else if (relatedType.equals("community")) {
            // 커뮤니티 댓글 조회 로직
            List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
            List<Comment> sortedComments = sortComments(comments);

            List<CommentListReponseDto> listReponse = new ArrayList<>();
            for (Comment comment : sortedComments) {
                // 댓글 작성자 조회
                Optional<Users> user = userRepository.findByUserNumber(comment.getUserNumber());
                String commentWriter = user.map(Users::getUserName).orElse("unknown");

                // 댓글 작성자 프로필 이미지 URL
                String imageUrl = imageRepository.findByRelatedTypeAndRelatedNumber("profile", comment.getUserNumber()).get().getUrl();

                // 답글 수 계산: 부모 댓글일 때만 계산
                long repliesCount = 0;
                if (comment.getParentNumber() == 0) {// 부모일 경우
                    repliesCount = commentRepository.countByRelatedTypeAndRelatedNumberAndParentNumber(relatedType, relatedNumber, comment.getCommentNumber()); // 답글 계산
                } else {
                    repliesCount = 0; //답글일 경우 답글 개수 0개
                }

                // 좋아요 상태 가져오기
                LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(likeRepository, "comment", comment.getCommentNumber(), userNumber);
                long likes = likeStatus.getTotalLikes();
                boolean liked = likeStatus.isLiked();

                //게시글 작성자 조회
                Optional<Community> postInfo = communityRepository.findByPostNumber(relatedNumber);
                int communityWritreNumber = postInfo.get().getUserNumber();

                //답글 작성 여부 가져오기
                Boolean commented = getCommented(comment, userNumber);

                // DTO 생성
                CommentListReponseDto dto = CommentListReponseDto.fromEntity(comment, commentWriter, repliesCount, likes, liked, communityWritreNumber, imageUrl, commented);
                listReponse.add(dto);
            }
            return listReponse;

            //travel도 community도 아닐 경우
        } else {
            throw new IllegalArgumentException("유효하지 않은 게시물 종류입니다: " + relatedType);
        }
    }

    //댓글 목록 조회 (페이징)
    @Transactional
    public Page<CommentListReponseDto> getListPage(PageRequest pageRequest, String relatedType, int relatedNumber, int userNumber) {

        //이때 userNumber는 댓글 조회 요청자

        if (relatedType.equals("travel")) {
            List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
            List<Comment> sortedComments = sortComments(comments);


            List<CommentListReponseDto> listReponse = new ArrayList<>();
            for (Comment comment : sortedComments) {

                //댓글 작성자 조회
                Optional<Users> user = userRepository.findByUserNumber(comment.getUserNumber());
                String commentWriter = user.map(Users::getUserName).orElse("unknown");

                // 댓글 작성자 프로필 이미지 URL
                String imageUrl = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder("profile", comment.getUserNumber(), 0).get().getUrl();

                // 답글 수 계산: 부모 댓글일 때만 계산
                long repliesCount = 0;
                if (comment.getParentNumber() == 0) {// 부모일 경우
                    repliesCount = commentRepository.countByRelatedTypeAndRelatedNumberAndParentNumber(relatedType, relatedNumber, comment.getCommentNumber()); // 답글 계산
                } else {
                    repliesCount = 0; //답글일 경우 답글 개수 0개
                }
                //좋아요 상태 가져오기
                LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(likeRepository, "comment", comment.getCommentNumber(), userNumber);
                //좋아요 개수
                long likes = likeStatus.getTotalLikes();
                //좋아요 여부, true = 좋아요 누름
                boolean liked = likeStatus.isLiked();

                //게시글 작성자 회원번보
                //게시글 정보 가져오기
                Optional<Travel> travelInfo = travelRepository.findByNumber(relatedNumber);
                int travelWriterNumber = travelInfo.get().getUserNumber();

                //답글 작성 여부 가져오기
                Boolean commented = getCommented(comment, userNumber);

                //DTO
                CommentListReponseDto dto = CommentListReponseDto.fromEntity(comment, commentWriter, repliesCount, likes, liked, travelWriterNumber, imageUrl, commented);
                listReponse.add(dto);
            }
            return toPage(listReponse, pageRequest);


        } else if (relatedType.equals("community")) {
            // 커뮤니티 댓글 조회 로직
            List<Comment> comments = commentRepository.findByRelatedTypeAndRelatedNumber(relatedType, relatedNumber);
            List<Comment> sortedComments = sortComments(comments);

            List<CommentListReponseDto> listReponse = new ArrayList<>();
            for (Comment comment : sortedComments) {
                // 댓글 작성자 조회
                Optional<Users> user = userRepository.findByUserNumber(comment.getUserNumber());
                String commentWriter = user.map(Users::getUserName).orElse("unknown");

                // 댓글 작성자 프로필 이미지 URL
                String imageUrl = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder("profile", comment.getUserNumber(), 0).get().getUrl();

                // 답글 수 계산: 부모 댓글일 때만 계산
                long repliesCount = 0;
                if (comment.getParentNumber() == 0) {// 부모일 경우
                    repliesCount = commentRepository.countByRelatedTypeAndRelatedNumberAndParentNumber(relatedType, relatedNumber, comment.getCommentNumber()); // 답글 계산
                } else {
                    repliesCount = 0; //답글일 경우 답글 개수 0개
                }

                // 좋아요 상태 가져오기
                LikeReadResponseDto likeStatus = LikeStatus.getLikeStatus(likeRepository, "comment", comment.getCommentNumber(), userNumber);
                long likes = likeStatus.getTotalLikes();
                boolean liked = likeStatus.isLiked();

                //게시글 작성자 조회
                Optional<Community> postInfo = communityRepository.findByPostNumber(relatedNumber);
                int communityWritreNumber = postInfo.get().getUserNumber();

                //답글 작성 여부 가져오기
                Boolean commented = getCommented(comment, userNumber);

                // DTO 생성
                CommentListReponseDto dto = CommentListReponseDto.fromEntity(comment, commentWriter, repliesCount, likes, liked, communityWritreNumber, imageUrl, commented);
                listReponse.add(dto);
            }
            return toPage(listReponse, pageRequest);

            //travel도 community도 아닐 경우
        } else {
            throw new IllegalArgumentException("유효하지 않은 게시물 종류입니다: " + relatedType);
        }
    }

    // update
    @Transactional
    public CommentDetailResponseDto update(CommentUpdateRequestDto request, int commentWriter, int commentNumber) {
        // 댓글 존재 여부 검증 검증
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다: " + commentNumber));

        // 댓글 작성자인지 확인
        validateCommentWriter(commentNumber, commentWriter);

        // 업데이트 동작
        comment.update(request.getContent()); // 수정할 내용을 설정

        // 업데이트 저장
        commentRepository.save(comment);

        // 업데이트된 댓글의 detail 리턴
        long likes = likeRepository.countByRelatedTypeAndRelatedNumber("comment", commentNumber);
        CommentDetailResponseDto result = new CommentDetailResponseDto(comment, likes);

        return result;
    }

    //Delete
    @Transactional
    public void delete(int commentNumber, int userNumber) {
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("comment not found: " + commentNumber));

        validateCommentWriterOrPostWriter(commentNumber, userNumber);

        try {
            // 답글 삭제
            List<Comment> replies = commentRepository.findByRelatedTypeAndRelatedNumberAndParentNumber(comment.getRelatedType(), comment.getRelatedNumber(), comment.getCommentNumber());
            for (Comment reply : replies) {
                try {
                    commentRepository.deleteByCommentNumber(reply.getCommentNumber());
                    likeRepository.deleteByRelatedTypeAndRelatedNumber("comment", reply.getCommentNumber());

                } catch (Exception e) {
                    log.error("Failed to delete reply comment: {}", reply.getCommentNumber(), e);
                }
            }

            // 좋아요 기록 삭제
            likeRepository.deleteByRelatedTypeAndRelatedNumber("comment", commentNumber);
            // 부모 댓글 삭제
            commentRepository.deleteByCommentNumber(commentNumber);
        } catch (Exception e) {
            log.error("댓글 삭제 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("댓글 삭제 실패: " + e.getMessage());
        }
    }


    //게시글 작성자인지 검증하는 메소드
    @Transactional(readOnly = true)
    public void validatePostWriter(int commentNumber, int userNumber) {

        //존재하는 댓글인지 확인
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("comment not found: " + commentNumber));

        int postWriterNumber = 0;
        if (comment.getRelatedType().equals("travel")) {
            // 댓글 번호로 게시글 번호 가져오기
            int travelNumber = comment.getRelatedNumber();
            // 게시글 번호로 작성자 회원 번호 가져오기
            postWriterNumber = travelRepository.findByNumber(travelNumber).get().getUserNumber();
        } else if (comment.getRelatedType().equals("community")) {
            int communityNumber = comment.getRelatedNumber();
            postWriterNumber = communityRepository.findByPostNumber(communityNumber).get().getUserNumber();
        }

        // 요청한 사용자(=로그인 중인 사용자)가 댓글 작성자 혹은 게시글 작성자인지 확인
        if (userNumber != postWriterNumber) {
            throw new IllegalArgumentException("게시글 작성자에게만 유효한 동작입니다.");
        }
    }


    //댓글 작성자인지 확인
    @Transactional(readOnly = true)
    public void validateCommentWriter(int commentNumber, int userNumber) {

        // 존재하는 댓글인지 확인
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("comment not found: " + commentNumber));

        // 요청한 사용자(=로그인 중인 사용자)가 댓글 작성자인지 확인
        if (comment.getUserNumber() != userNumber) {
            throw new IllegalArgumentException("댓글 작성자에게만 유효한 동작입니다.");
        }
    }

    // 댓글 작성자 혹은 게시글 작성자인지 검증하는 메소드
    @Transactional(readOnly = true)
    public void validateCommentWriterOrPostWriter(int commentNumber, int userNumber) {

        //존재하는 댓글인지 확인
        Comment comment = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new IllegalArgumentException("comment not found: " + commentNumber));

        int postWriterNumber = 0;
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
        if (userNumber != postWriterNumber | commentWriter!= userNumber) {
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
    private Boolean getCommented (Comment comment, int userNumber) {
        //답글 작성 여부 가져오기
        Boolean commented = null;
        //나의 자식 댓글들 조회
        List<Comment> childComments = commentRepository.findByRelatedTypeAndRelatedNumberAndParentNumber(comment.getRelatedType(), comment.getRelatedNumber(), comment.getCommentNumber());
        if (!childComments.isEmpty()) { //자식 답글이 있을 경우
            commented = false; //
            //자식 답글들을 순회하며 조회 중인 유저 번호와 일치하는지 확인
            for (Comment childComment : childComments) {
                if (childComment.getUserNumber() == userNumber) {
                    //하나라도 일치할 경우 commented를 true하고 for문 빠져나오기
                    commented = true;
                    break;
                }
            }
        }
        return commented;
    }
}