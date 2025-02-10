package swyp.swyp6_team7.notification.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import swyp.swyp6_team7.notification.entity.Notification;
import swyp.swyp6_team7.notification.entity.TravelNotification;
import swyp.swyp6_team7.notification.repository.NotificationRepository;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class NotificationDeleteServiceTest {

    @Autowired
    private NotificationDeleteService notificationDeleteService;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private DateTimeProvider dateTimeProvider;
    @SpyBean
    private AuditingHandler handler;
    @SpyBean
    private Clock clockMock;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        handler.setDateTimeProvider(dateTimeProvider);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
    }

    @DisplayName("오래된 알림 삭제 작업은 45일 이전에 생성된 알림을 삭제한다.")
    @Test
    void deleteNotifications() {
        // given
        LocalDateTime testTime1 = LocalDateTime.of(2025, 1, 9, 23, 59);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(testTime1));
        Notification notification1 = notificationRepository.save(createNotification(true));
        Notification notification2 = notificationRepository.save(createNotification(false));

        LocalDateTime testTime2 = LocalDateTime.of(2025, 2, 10, 0, 0);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(testTime2));
        Notification notification3 = notificationRepository.save(createNotification(true));

        when(clockMock.instant()).thenReturn(Instant.parse("2025-03-27T00:00:00Z"));

        // when
        notificationDeleteService.deleteNotifications();

        // then
        assertThat(notificationRepository.findAll()).hasSize(1)
                .extracting("number")
                .containsExactly(notification3.getNumber());
    }

    @DisplayName("오래된 알림 삭제 작업은 매일 새벽 2시에 스케줄러로 실행된다.")
    @Test
    void deleteNotificationsScheduler() {
        // given
        String methodName = "deleteNotifications";
        CronTrigger trigger = getTriggerFromMethod(NotificationDeleteService.class, methodName);

        LocalDateTime initialTime = LocalDateTime.of(2025, 2, 9, 0, 0);
        Instant instant = toInstant(initialTime);
        SimpleTriggerContext context = new SimpleTriggerContext(instant, instant, instant);

        List<Instant> expectedTimes = List.of(
                LocalDateTime.of(2025, 2, 9, 2, 0),
                LocalDateTime.of(2025, 2, 10, 2, 0),
                LocalDateTime.of(2025, 2, 11, 2, 0)
        ).stream().map(this::toInstant).toList();

        // when // then
        for (Instant expectedTime : expectedTimes) {
            Instant actual = trigger.nextExecution(context);
            assertThat(actual).isEqualTo(expectedTime);
            context.update(actual, actual, actual);
        }
    }


    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    private CronTrigger getTriggerFromMethod(Class<?> targetClass, String methodName) {
        Method method = ReflectionUtils.findMethod(targetClass, methodName).get();
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        return createTrigger(scheduled);
    }

    private CronTrigger createTrigger(Scheduled scheduled) {
        if (StringUtils.isNotBlank(scheduled.zone())) {
            return new CronTrigger(scheduled.cron(), TimeZone.getTimeZone(scheduled.zone()));
        } else {
            return new CronTrigger(scheduled.cron());
        }
    }

    private Notification createNotification(boolean isRead) {
        return notificationRepository.save(TravelNotification.builder()
                .travelNumber(1)
                .receiverNumber(1)
                .isRead(isRead)
                .build());
    }

}
