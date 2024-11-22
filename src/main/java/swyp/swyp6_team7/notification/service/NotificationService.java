package swyp.swyp6_team7.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.notification.dto.NotificationDto;
import swyp.swyp6_team7.notification.dto.TravelCommentNotificationDto;
import swyp.swyp6_team7.notification.dto.TravelNotificationDto;
import swyp.swyp6_team7.notification.entity.Notification;
import swyp.swyp6_team7.notification.entity.TravelCommentNotification;
import swyp.swyp6_team7.notification.entity.TravelNotification;
import swyp.swyp6_team7.notification.repository.NotificationRepository;
import swyp.swyp6_team7.notification.util.NotificationMaker;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TravelRepository travelRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BookmarkRepository bookmarkRepository;


    @Async
    public void createEnrollNotification(Travel targetTravel, int enrollUserNumber) {
        // to 주최자
        Notification newNotificationToHost = NotificationMaker.travelEnrollmentMessageToHost(targetTravel);
        Notification createdNotificationToHost = notificationRepository.save(newNotificationToHost);
        log.info("여행 참가 신청 HOST 알림 - receiverNumber: {}, notificationNumber: {}", targetTravel.getUserNumber(), createdNotificationToHost.getNumber());

        // to 신청자
        Notification newNotification = NotificationMaker.travelEnrollmentMessage(targetTravel, enrollUserNumber);
        Notification createdNotification = notificationRepository.save(newNotification);
        log.info("여행 참가 신청 요청자 알림 - receiverNumber: {}, notificationNumber: {}", enrollUserNumber, createdNotification.getNumber());
    }

    @Async
    public void createAcceptNotification(Travel targetTravel, int enrollUserNumber) {
        Notification newNotification = NotificationMaker.travelAcceptMessage(targetTravel, enrollUserNumber);
        Notification createdNotification = notificationRepository.save(newNotification);
        log.info("여행 참가 신청 수락 알림 - receiverNumber: {}, notificationNumber: {}", enrollUserNumber, createdNotification.getNumber());
    }

    @Async
    public void createRejectNotification(Travel targetTravel, int enrollUserNumber) {
        Notification newNotification = NotificationMaker.travelRejectMessage(targetTravel, enrollUserNumber);
        Notification createdNotification = notificationRepository.save(newNotification);
        log.info("여행 참가 신청 수락 알림 - receiverNumber: {}, notificationNumber: {}", enrollUserNumber, createdNotification.getNumber());
    }

    @Async
    public void createCompanionClosedNotification(Travel targetTravel) {
        List<Notification> createdNotifications = new ArrayList<>();

        // to 주최자
        Notification hostNotification = NotificationMaker.travelCompanionClosedMessageToHost(targetTravel);
        createdNotifications.add(hostNotification);

        // to 참가 확정자
        List<Integer> companionsNumber = targetTravel.getCompanions().stream()
                .map(companion -> companion.getUserNumber())
                .toList();
        List<Notification> notificationsToCompanions = companionsNumber.stream()
                .map(userNumber -> NotificationMaker.travelClosedMessageToCompanions(targetTravel, userNumber))
                .collect(Collectors.toList());
        createdNotifications.addAll(notificationsToCompanions);

        // to PENDING 신청자
        List<Integer> pendingUsersNumber = enrollmentRepository.findUserNumbersByTravelNumberAndStatus(targetTravel.getNumber(), EnrollmentStatus.PENDING);
        List<Notification> notificationsToPendingUser = pendingUsersNumber.stream()
                .map(userNumber -> NotificationMaker.travelClosedMessageToPendingUser(targetTravel, userNumber))
                .collect(Collectors.toList());
        createdNotifications.addAll(notificationsToPendingUser);

        // to 즐겨찾기(북마크) 사용자
        List<Integer> bookmarkedUsersNumber = bookmarkRepository.findUserNumberByTravelNumber(targetTravel.getNumber())
                        .stream().collect(Collectors.toList());
        bookmarkedUsersNumber.removeAll(new HashSet<>(companionsNumber));
        bookmarkedUsersNumber.removeAll(new HashSet<>(pendingUsersNumber));

        List<Notification> notificationsToBookmarkedUsers = bookmarkedUsersNumber.stream()
                .filter(userNumber -> userNumber != targetTravel.getUserNumber())
                .map(userNumber -> NotificationMaker.travelClosedMessageToBookmarkedUser(targetTravel, userNumber))
                .collect(Collectors.toList());
        createdNotifications.addAll(notificationsToBookmarkedUsers);

        notificationRepository.saveAll(createdNotifications);
    }

    @Async
    public void createCommentNotifications(Integer requestUserNumber, String relatedType, Integer relatedNumber) {
        if (!relatedType.equals("travel")) {
            return;
        }

        Travel targetTravel = travelRepository.findByNumber(relatedNumber)
                .orElseThrow(() -> {
                    log.warn("new comment notification - 존재하지 않는 여행 콘텐츠입니다. travelNumber: {}", relatedNumber);
                    return new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
                });

        // to 주최자 (댓글 작성자가 주최자가 아닌 경우에만 주최자용 알림 생성)
        if (requestUserNumber != targetTravel.getUserNumber()) {
            notificationRepository.save(NotificationMaker.travelNewCommentMessageToHost(targetTravel));
        }

        // to 신청자 (작성자는 알림 생성 제외)
        List<Integer> enrolledUserNumbers = enrollmentRepository.findUserNumbersByTravelNumberAndStatus(targetTravel.getNumber(), EnrollmentStatus.ACCEPTED);
        List<TravelCommentNotification> createdNotifications = enrolledUserNumbers.stream()
                .distinct()
                .filter(userNumber -> userNumber != requestUserNumber)
                .map(userNumber -> NotificationMaker.travelNewCommentMessageToEnrollments(targetTravel, userNumber))
                .toList();
        notificationRepository.saveAll(createdNotifications);
    }


    public Page<NotificationDto> getNotificationsByUser(PageRequest pageRequest, int requestUserNumber) {

        Page<Notification> notifications = notificationRepository
                .getNotificationsByReceiverNumberOrderByIsReadAscCreatedAtDesc(pageRequest, requestUserNumber);

        return notifications.map(notification -> makeDto(notification));
    }

    private NotificationDto makeDto(Notification notification) {
        NotificationDto result;

        if (notification instanceof TravelNotification) {
            TravelNotification travelNotification = (TravelNotification) notification;
            result = new TravelNotificationDto(travelNotification);
        } else if (notification instanceof TravelCommentNotification) {
            TravelCommentNotification travelCommentNotification = (TravelCommentNotification) notification;
            result = new TravelCommentNotificationDto(travelCommentNotification);
        } else {
            result = new NotificationDto(notification);
        }
        changeReadStatus(notification);
        return result;
    }

    private void changeReadStatus(Notification notification) {
        if (!notification.getIsRead()) {
            notification.read();
        }
    }

}
