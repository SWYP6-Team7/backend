package swyp.swyp6_team7.notification.dto;

import lombok.Getter;
import swyp.swyp6_team7.notification.entity.TravelCommentNotification;

@Getter
public class TravelCommentNotificationDto extends NotificationDto {

    private Integer travelNumber;

    public TravelCommentNotificationDto(TravelCommentNotification notification) {
        super(notification);
        this.travelNumber = notification.getTravelNumber();
    }
}
