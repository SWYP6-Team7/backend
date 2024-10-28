package swyp.swyp6_team7.notification.util;

import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.notification.entity.Notification;
import swyp.swyp6_team7.notification.entity.NotificationMessageType;
import swyp.swyp6_team7.notification.entity.TravelCommentNotification;
import swyp.swyp6_team7.notification.entity.TravelNotification;
import swyp.swyp6_team7.travel.domain.Travel;

public class NotificationMaker {

    public static Notification travelEnrollmentMessageToHost(Travel targetTravel) {
        return TravelNotification.builder()
                .receiverNumber(targetTravel.getUserNumber())
                .title(NotificationMessageType.TRAVEL_ENROLL_HOST.getTitle())
                .content(NotificationMessageType.TRAVEL_ENROLL_HOST.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .travelTitle(targetTravel.getTitle())
                .travelDueDate(targetTravel.getDueDate())
                .isRead(false)
                .build();
    }

    public static Notification travelEnrollmentMessage(Travel targetTravel, Users users) {
        return TravelNotification.builder()
                .receiverNumber(users.getUserNumber())
                .title(NotificationMessageType.TRAVEL_ENROLL.getTitle())
                .content(NotificationMessageType.TRAVEL_ENROLL.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .travelTitle(targetTravel.getTitle())
                .travelDueDate(targetTravel.getDueDate())
                .isRead(false)
                .build();
    }

    public static Notification travelAcceptMessage(Travel targetTravel, Enrollment enrollment) {
        return TravelNotification.builder()
                .receiverNumber(enrollment.getUserNumber())
                .title(NotificationMessageType.TRAVEL_ACCEPT.getTitle())
                .content(NotificationMessageType.TRAVEL_ACCEPT.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .travelTitle(targetTravel.getTitle())
                .travelDueDate(targetTravel.getDueDate())
                .isRead(false)
                .build();
    }

    public static Notification travelRejectMessage(Travel targetTravel, Enrollment enrollment) {
        return TravelNotification.builder()
                .receiverNumber(enrollment.getUserNumber())
                .title(NotificationMessageType.TRAVEL_REJECT.getTitle())
                .content(NotificationMessageType.TRAVEL_REJECT.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .travelTitle(targetTravel.getTitle())
                .travelDueDate(targetTravel.getDueDate())
                .isRead(false)
                .build();
    }

    public static TravelCommentNotification travelNewCommentMessageToHost(Travel targetTravel) {
        return TravelCommentNotification.builder()
                .receiverNumber(targetTravel.getUserNumber())
                .title(NotificationMessageType.TRAVEL_NEW_COMMENT_HOST.getTitle())
                .content(NotificationMessageType.TRAVEL_NEW_COMMENT_HOST.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .isRead(false)
                .build();
    }

    public static TravelCommentNotification travelNewCommentMessageToEnrollments(Travel targetTravel, int enrolledUserNumber) {
        return TravelCommentNotification.builder()
                .receiverNumber(enrolledUserNumber)
                .title(NotificationMessageType.TRAVEL_NEW_COMMENT_ENROLLMENT.getTitle())
                .content(NotificationMessageType.TRAVEL_NEW_COMMENT_ENROLLMENT.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .isRead(false)
                .build();
    }

}
