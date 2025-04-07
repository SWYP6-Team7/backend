package swyp.swyp6_team7.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.companion.repository.CompanionRepository;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.notification.entity.CommunityCommentNotification;
import swyp.swyp6_team7.notification.entity.TravelCommentNotification;
import swyp.swyp6_team7.notification.repository.NotificationRepository;
import swyp.swyp6_team7.notification.util.NotificationMaker;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentNotificationService {

    private final NotificationRepository notificationRepository;
    private final TravelRepository travelRepository;
    private final CompanionRepository companionRepository;
    private final CommunityRepository communityRepository;

    @Transactional
    public void createTravelCommentNotification(Integer requestUserNumber, Integer relatedNumber) {
        Travel targetTravel = travelRepository.findByNumber(relatedNumber)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 여행 콘텐츠입니다: travelNumber={}", relatedNumber);
                    return new MoingApplicationException("존재하지 않는 여행 콘텐츠입니다.");
                });

        // to 주최자 (댓글 작성자가 주최자가 아닌 경우에만 주최자용 알림 생성)
        if (requestUserNumber != targetTravel.getUserNumber()) {
            notificationRepository.save(NotificationMaker.travelNewCommentMessageToHost(targetTravel));
        }

        // to Companion(참가 확정자)
        List<Integer> companionUserNumbers = companionRepository.getUserNumbersByTravelNumber(targetTravel.getNumber());
        List<TravelCommentNotification> createdNotifications = companionUserNumbers.stream()
                .distinct()
                .filter(userNumber -> userNumber != requestUserNumber)
                .map(userNumber -> NotificationMaker.travelNewCommentMessageToCompanions(targetTravel, userNumber))
                .toList();
        notificationRepository.saveAll(createdNotifications);
    }

    @Transactional
    public void createCommunityCommentNotification(Integer requestUserNumber, Integer relatedNumber) {
        Community targetPost = communityRepository.findByPostNumber(relatedNumber)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 커뮤니티 게시글입니다: postNumber={}", relatedNumber);
                    return new MoingApplicationException("존재하지 않는 커뮤니티 게시글입니다.");
                });

        // 댓글 작성자가 커뮤니티 게시글 작성자인 경우 알림 미생성
        if (requestUserNumber == targetPost.getUserNumber()) {
            return;
        }

        // to 게시물 작성자
        // 기존 댓글 알림이 있는 경우 -> 기존 데이터를 이용해 새로 알림을 생성
        CommunityCommentNotification notification = notificationRepository
                .findCommunityCommentNotificationByPostNumber(targetPost.getPostNumber());

        CommunityCommentNotification newNotification;
        if (notification == null) {
            newNotification = CommunityCommentNotification.create(targetPost, 1);
        } else {
            newNotification = CommunityCommentNotification.create(targetPost, notification.getNotificationCount() + 1);
            notificationRepository.delete(notification);
        }
        notificationRepository.save(newNotification);
    }
}
