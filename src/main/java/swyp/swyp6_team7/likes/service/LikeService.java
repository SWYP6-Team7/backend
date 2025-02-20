package swyp.swyp6_team7.likes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.repository.CommentRepository;
import swyp.swyp6_team7.comment.service.CommentService;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.community.service.CommunityService;
import swyp.swyp6_team7.likes.domain.Like;
import swyp.swyp6_team7.likes.repository.LikeRepository;
import swyp.swyp6_team7.notification.service.LikeNotificationService;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final CommunityRepository communityRepository;
    private final CommunityService communityService;
    private final LikeNotificationService likeNotificationService;

    // TODO: Response Type 일치 및 상속구조 구현
    @Transactional
    public Object toggleLike(String relatedType, int relatedNumber, int userNumber) {

        //댓글 좋아요의 경우
        if ("comment".equals(relatedType)) {
            // 댓글 존재 여부 검증
            Comment comment = commentRepository.findByCommentNumber(relatedNumber)
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다: " + relatedNumber));

            // 좋아요 여부 확인
            boolean liked = likeRepository.existsByRelatedTypeAndRelatedNumberAndUserNumber(relatedType, relatedNumber, userNumber);

            if (liked) { // 이미 좋아요를 누른 경우
                // 좋아요 취소
                Optional<Like> commentLike = likeRepository.findByRelatedTypeAndRelatedNumberAndUserNumber(relatedType, relatedNumber, userNumber);
                commentLike.ifPresent(cl -> {
                    likeRepository.delete(cl); // 좋아요 삭제
                });
            } else { // 좋아요를 누르지 않은 경우
                // 좋아요 추가
                Like like = new Like(relatedType, relatedNumber, userNumber);
                likeRepository.save(like);
            }
            return commentService.getList(comment.getRelatedType(), comment.getRelatedNumber(), userNumber);

            //게시물 좋아요의 경우
        } else if ("community".equals(relatedType)) {

            //게시글 존재 여부 검증
            Community community = communityRepository.findByPostNumber(relatedNumber)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: 커뮤니티 게시글 : " + relatedNumber));
            // 좋아요 여부 확인
            boolean liked = likeRepository.existsByRelatedTypeAndRelatedNumberAndUserNumber(relatedType, relatedNumber, userNumber);

            if (liked) { // 이미 좋아요를 누른 경우
                // 좋아요 취소
                Optional<Like> like = likeRepository.findByRelatedTypeAndRelatedNumberAndUserNumber(relatedType, relatedNumber, userNumber);
                like.ifPresent(cl -> {
                    likeRepository.delete(cl); // 좋아요 삭제
                });
            } else { // 좋아요를 누르지 않은 경우
                // 좋아요 추가
                Like like = new Like(relatedType, relatedNumber, userNumber);
                likeRepository.save(like);
                if (userNumber != community.getUserNumber()) {
                    likeNotificationService.createCommunityPostLikeNotification(community); // 좋아요 알림
                }
            }
            return communityService.getDetail(community.getPostNumber(), userNumber);

        } else {
            throw new IllegalArgumentException("유효하지 않은 relatedType: " + relatedType);
        }
    }
}
