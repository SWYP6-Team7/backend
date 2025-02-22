package swyp.swyp6_team7.notification.util;

import swyp.swyp6_team7.notification.entity.Notification;
import swyp.swyp6_team7.notification.entity.NotificationMessageType;
import swyp.swyp6_team7.notification.entity.TravelCommentNotification;
import swyp.swyp6_team7.notification.entity.TravelNotification;
import swyp.swyp6_team7.travel.domain.Travel;

public class NotificationMaker {

    public static Notification travelEnrollmentMessageToHost(Travel targetTravel) {
        return createHostTravelNotification(NotificationMessageType.TRAVEL_ENROLL_HOST, targetTravel);
    }

    public static Notification travelEnrollmentMessage(Travel targetTravel, int receiveUserNumber) {
        return createCommonTravelNotification(NotificationMessageType.TRAVEL_ENROLL, targetTravel, receiveUserNumber);
    }

    public static Notification travelAcceptMessage(Travel targetTravel, int receiveUserNumber) {
        return createCommonTravelNotification(NotificationMessageType.TRAVEL_ACCEPT, targetTravel, receiveUserNumber);
    }

    public static Notification travelRejectMessage(Travel targetTravel, int receiveUserNumber) {
        return createCommonTravelNotification(NotificationMessageType.TRAVEL_REJECT, targetTravel, receiveUserNumber);
    }

    public static Notification travelCompanionClosedMessageToHost(Travel targetTravel) {
        return createHostTravelNotification(NotificationMessageType.TRAVEL_COMPANION_CLOSED_HOST, targetTravel);
    }

    public static Notification travelClosedMessageToCompanions(Travel targetTravel, int receiveUserNumber) {
        return createCommonTravelNotification(NotificationMessageType.TRAVEL_CLOSED_COMPANION, targetTravel, receiveUserNumber);
    }

    public static Notification travelClosedMessageToPendingUser(Travel targetTravel, int receiveUserNumber) {
        return createCommonTravelNotification(NotificationMessageType.TRAVEL_CLOSED_PENDING, targetTravel, receiveUserNumber);
    }

    public static Notification travelClosedMessageToBookmarkedUser(Travel targetTravel, int receiveUserNumber) {
        return createCommonTravelNotification(NotificationMessageType.TRAVEL_CLOSED_BOOKMARKED, targetTravel, receiveUserNumber);
    }

    private static TravelNotification createHostTravelNotification(NotificationMessageType messageType, Travel targetTravel) {
        return TravelNotification.builder()
                .receiverNumber(targetTravel.getUserNumber())
                .title(messageType.getTitle())
                .content(messageType.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .travelTitle(targetTravel.getTitle())
                .travelHost(true)
                .isRead(false)
                .build();
    }

    private static TravelNotification createCommonTravelNotification(NotificationMessageType messageType, Travel targetTravel, int receiveUserNumber) {
        return TravelNotification.builder()
                .receiverNumber(receiveUserNumber)
                .title(messageType.getTitle())
                .content(messageType.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .travelTitle(targetTravel.getTitle())
                .travelHost(false)
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

    public static TravelCommentNotification travelNewCommentMessageToCompanions(Travel targetTravel, int companionUserNumber) {
        return TravelCommentNotification.builder()
                .receiverNumber(companionUserNumber)
                .title(NotificationMessageType.TRAVEL_NEW_COMMENT_COMPANION.getTitle())
                .content(NotificationMessageType.TRAVEL_NEW_COMMENT_COMPANION.getContent(targetTravel.getTitle()))
                .travelNumber(targetTravel.getNumber())
                .isRead(false)
                .build();
    }

}
