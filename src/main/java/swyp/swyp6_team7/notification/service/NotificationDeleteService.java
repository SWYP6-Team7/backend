package swyp.swyp6_team7.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.notification.repository.NotificationRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationDeleteService {

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    // 오래된 알림 삭제 작업 (매일 새벽 2시 실행)
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteNotifications() {
        log.info("SCHEDULER::DAILY: Notification 삭제 작업 시작");

        // 오래된(45일 이전 생성된) 알림 목록 조회
        LocalDateTime cutOffDateTime = LocalDate.now(clock).minusDays(45).atStartOfDay();
        List<Long> notificationNumbers = notificationRepository.getNumbersByCreatedBefore(cutOffDateTime);

        notificationRepository.deleteAllByNumbers(notificationNumbers);

        log.info("SCHEDULER::DAILY: Notification 삭제 작업 종료");
    }
}
