package swyp.swyp6_team7.notification.dto;

import lombok.Getter;
import swyp.swyp6_team7.notification.entity.CommunityPostLikeNotification;

@Getter
public class CommunityPostLikeNotificationDto extends NotificationDto {

    private Integer communityNumber;

    public CommunityPostLikeNotificationDto(CommunityPostLikeNotification notification) {
        super(notification);
        this.communityNumber = notification.getCommunityNumber();
    }
}
