package swyp.swyp6_team7.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.notification.entity.UserBlockWarnNotification;
import swyp.swyp6_team7.notification.repository.NotificationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBlockWarnNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createUserBlockWarnNotification(int reportedUserNumber, int reportCount) {
        log.info("유저 신고 주의 알림 발송. 대상유저 : {}", reportedUserNumber);

        UserBlockWarnNotification notification = UserBlockWarnNotification.create(reportedUserNumber, reportCount);
        notificationRepository.save(notification);
    }
}
