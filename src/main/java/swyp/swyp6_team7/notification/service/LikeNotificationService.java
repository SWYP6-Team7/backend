package swyp.swyp6_team7.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.notification.entity.CommunityPostLikeNotification;
import swyp.swyp6_team7.notification.repository.NotificationRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeNotificationService {

    private final NotificationRepository notificationRepository;

    // Community Post Like 알림 생성
    @Transactional
    public void createCommunityPostLikeNotification(Community targetPost) {
        CommunityPostLikeNotification notification = notificationRepository
                .findCommunityPostLikeNotificationByPostNumber(targetPost.getPostNumber());

        // to 게시물 작성자
        // 기존 좋아요 알림이 있는 경우 -> 기존 데이터를 이용해 새로 알림을 생성
        CommunityPostLikeNotification newNotification;
        if (notification == null) {
            newNotification = CommunityPostLikeNotification.create(targetPost, 1);
        } else {
            newNotification = CommunityPostLikeNotification.create(targetPost, notification.getNotificationCount() + 1);
            notificationRepository.delete(notification);
        }
        notificationRepository.save(newNotification);
    }

}
