package swyp.swyp6_team7.notification.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.notification.entity.Notification;
import swyp.swyp6_team7.notification.entity.TravelNotification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@DataJpaTest
@Import(DataConfig.class)
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;
    @MockBean
    private DateTimeProvider dateTimeProvider;
    @SpyBean
    private AuditingHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        handler.setDateTimeProvider(dateTimeProvider);
    }


    @DisplayName("getNotifications: isRead가 false인 알림이 우선정렬된다")
    @Test
    public void getNotificationsByReceiverNumber() {
        // given
        LocalDateTime testTime1 = LocalDateTime.of(2024, 9, 25, 10, 0);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(testTime1));
        Notification readNotification1 = notificationRepository.save(createNotification(true));
        Notification unreadNotification1 = notificationRepository.save(createNotification(false));

        LocalDateTime testTime2 = LocalDateTime.of(2024, 9, 30, 10, 0);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(testTime2));
        Notification readNotification2 = notificationRepository.save(createNotification(true));
        Notification unreadNotification2 = notificationRepository.save(createNotification(false));

        PageRequest pageRequest = PageRequest.of(0, 5);

        // when
        Page<Notification> notifications = notificationRepository
                .getNotificationsByReceiverNumberOrderByIsReadAscCreatedAtDesc(pageRequest, 1);

        // then
        for (Notification notification : notifications.getContent()) {
            System.out.println(notification.toString());
        }
        assertThat(notifications.getTotalElements()).isEqualTo(4);
        assertThat(notifications.getContent().get(0)).isEqualTo(unreadNotification2);
        assertThat(notifications.getContent().get(1)).isEqualTo(unreadNotification1);
        assertThat(notifications.getContent().get(2)).isEqualTo(readNotification2);
        assertThat(notifications.getContent().get(3)).isEqualTo(readNotification1);
    }

    @DisplayName("기준 날짜시간보다 이전에 생성된 알림의 식별자 목록을 가져온다.")
    @Test
    void getNumbersByCreatedBefore() {
        // given
        LocalDateTime cutOffDateTime = LocalDateTime.of(2025, 2, 10, 0, 0);

        LocalDateTime testTime1 = LocalDateTime.of(2025, 2, 9, 23, 59);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(testTime1));
        Notification notification1 = notificationRepository.save(createNotification(true));
        Notification notification2 = notificationRepository.save(createNotification(false));

        LocalDateTime testTime2 = LocalDateTime.of(2025, 2, 10, 0, 0);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(testTime2));
        Notification notification3 = notificationRepository.save(createNotification(true));

        // when
        List<Long> result = notificationRepository.getNumbersByCreatedBefore(cutOffDateTime);

        // then
        assertThat(result).hasSize(2)
                .containsExactly(notification1.getNumber(), notification2.getNumber());
    }

    @DisplayName("특정 사용자의 알림 중 가장 최근에 생성된 것을 한 개 가져온다.")
    @Test
    void findTopByReceiverNumberOrderByCreatedAtDesc() {
        // given
        LocalDateTime time1 = LocalDateTime.of(2025, 2, 14, 0, 0);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(time1));
        Notification notification1 = notificationRepository.save(createNotification(true));

        LocalDateTime time2 = LocalDateTime.of(2025, 2, 14, 12, 0);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(time2));
        Notification notification2 = notificationRepository.save(createNotification(true));

        // when
        Notification result = notificationRepository.findTopByReceiverNumberOrderByCreatedAtDesc(1);

        // then
        assertThat(result).isEqualTo(notification2);
    }

    @DisplayName("알림 식별자 목록이 주어질 때 해당 알림들을 삭제한다.")
    @Test
    void deleteAllByNumbers() {
        // given
        LocalDateTime testTime = LocalDateTime.of(2025, 2, 9, 0, 0);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(testTime));

        List<Long> numbers = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Notification notification = notificationRepository.save(createNotification(true));
            numbers.add(notification.getNumber());
        }
        for (int i = 0; i < 50; i++) {
            Notification notification = notificationRepository.save(createNotification(true));
        }

        // when
        notificationRepository.deleteAllByNumbers(numbers);

        // then
        assertThat(notificationRepository.findAll()).hasSize(50)
                .extracting("number")
                .doesNotContain(numbers);
    }

    private Notification createNotification(boolean isRead) {
        return notificationRepository.save(TravelNotification.builder()
                .travelNumber(1)
                .receiverNumber(1)
                .isRead(isRead)
                .build());
    }
}
