package swyp.swyp6_team7.notification.dto;

import lombok.Getter;
import swyp.swyp6_team7.notification.entity.TravelNotification;

@Getter
public class TravelNotificationDto extends NotificationDto {

    private Integer travelNumber;
    private String travelTitle;
    private Boolean travelHostUser;

    public TravelNotificationDto(TravelNotification notification) {
        super(notification);
        this.travelNumber = notification.getTravelNumber();
        this.travelTitle = notification.getTravelTitle();
        this.travelHostUser = notification.getTravelHost();
    }

}
