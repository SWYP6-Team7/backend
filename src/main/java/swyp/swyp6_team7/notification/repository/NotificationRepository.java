package swyp.swyp6_team7.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import swyp.swyp6_team7.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> getNotificationsByReceiverNumberOrderByIsReadAscCreatedAtDesc(Pageable pageable, int userNumber);

}
