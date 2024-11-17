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

        Notification readNotification1 = notificationRepository.save(TravelNotification.builder()
                .travelNumber(1)
                .receiverNumber(1)
                .isRead(true)
                .build());

        Notification unreadNotification1 = notificationRepository.save(TravelNotification.builder()
                .travelNumber(1)
                .receiverNumber(1)
                .isRead(false)
                .build());

        LocalDateTime testTime2 = LocalDateTime.of(2024, 9, 30, 10, 0);
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(testTime2));

        Notification readNotification2 = notificationRepository.save(TravelNotification.builder()
                .travelNumber(1)
                .receiverNumber(1)
                .isRead(true)
                .build());

        Notification unreadNotification2 = notificationRepository.save(TravelNotification.builder()
                .travelNumber(1)
                .receiverNumber(1)
                .isRead(false)
                .build());

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

}