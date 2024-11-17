package swyp.swyp6_team7.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;
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

import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TravelRepository travelRepository;
    private final EnrollmentRepository enrollmentRepository;


    @Async
    public void createEnrollNotification(Travel targetTravel, int enrollUserNumber) {
        // notification to host
        Notification newNotificationToHost = NotificationMaker.travelEnrollmentMessageToHost(targetTravel);
        Notification createdNotificationToHost = notificationRepository.save(newNotificationToHost);
        log.info("여행 참가 신청 HOST 알림 - receiverNumber: {}, notificationNumber: {}", targetTravel.getUserNumber(), createdNotificationToHost.getNumber());

        // notification to 신청자
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
    public void createCommentNotifications(Integer requestUserNumber, String relatedType, Integer relatedNumber) {
        if (!relatedType.equals("travel")) {
            return;
        }

        Travel targetTravel = travelRepository.findByNumber(relatedNumber)
                .orElseThrow(() -> new IllegalArgumentException("Travel Not Found"));

        // notification to host (작성자가 host인 경우 host 알림 생성 제외)
        if (requestUserNumber != targetTravel.getUserNumber()) {
            notificationRepository.save(NotificationMaker.travelNewCommentMessageToHost(targetTravel));
        }

        // notification to each enrollment (작성자 자신에게는 알림 생성 제외)
        List<Integer> enrolledUserNumbers = enrollmentRepository.findEnrolledUserNumbersByTravelNumber(targetTravel.getNumber());
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
