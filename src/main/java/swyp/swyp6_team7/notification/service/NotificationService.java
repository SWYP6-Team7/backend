package swyp.swyp6_team7.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.notification.dto.*;
import swyp.swyp6_team7.notification.entity.*;
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
    private final CommunityRepository communityRepository;


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
        // TODO: 메서드 OR 서비스 클래스 분리
        if (relatedType.equals("travel")) {
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

        } else if (relatedType.equals("community")) {
            Community targetPost = communityRepository.findByPostNumber(relatedNumber)
                    .orElseThrow(() -> {
                        log.warn("new comment notification - 존재하지 않는 커뮤니티 게시글입니다. postNumber: {}", relatedNumber);
                        return new IllegalArgumentException("존재하지 않는 커뮤니티 게시글입니다.");
                    });

            if (requestUserNumber == targetPost.getUserNumber()) {
                return;
            }

            // to 게시물 작성자
            // 기존 댓글 알림이 있는 경우 -> 기존 데이터를 이용해 새로 알림을 생성
            CommunityCommentNotification notification = notificationRepository
                    .findCommunityCommentNotificationByPostNumber(targetPost.getPostNumber());

            CommunityCommentNotification newNotification;
            if (notification == null) {
                newNotification = CommunityCommentNotification.create(targetPost, 1);
            } else {
                newNotification = CommunityCommentNotification.create(targetPost, notification.getNotificationCount() + 1);
                notificationRepository.delete(notification);
            }
            notificationRepository.save(newNotification);
        }
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
        } else if (notification instanceof CommunityCommentNotification) {
            CommunityCommentNotification communityCommentNotification = (CommunityCommentNotification) notification;
            result = new CommunityCommentNotificationDto(communityCommentNotification);
        } else if (notification instanceof CommunityPostLikeNotification) {
            CommunityPostLikeNotification communityCommentNotification = (CommunityPostLikeNotification) notification;
            result = new CommunityPostLikeNotificationDto(communityCommentNotification);
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
