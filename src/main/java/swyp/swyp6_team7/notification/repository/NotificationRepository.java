package swyp.swyp6_team7.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.swyp6_team7.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> getNotificationsByReceiverNumberOrderByIsReadAscCreatedAtDesc(Pageable pageable, int userNumber);
    Notification findTopByReceiverNumberOrderByCreatedAtDesc(int userNumber);

    @Query("select n.number from Notification n where n.createdAt < :cutOffDateTime")
    List<Long> getNumbersByCreatedBefore(@Param("cutOffDateTime") LocalDateTime cutOffDateTime);

    @Query("delete from Notification n where n.number in :notificationNumbers")
    @Modifying(clearAutomatically = true)
    void deleteAllByNumbers(@Param("notificationNumbers") List<Long> notificationNumbers);
}
