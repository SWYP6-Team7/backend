package swyp.swyp6_team7.notification.dto;

import lombok.Getter;
import swyp.swyp6_team7.notification.entity.CommunityCommentNotification;

@Getter
public class CommunityCommentNotificationDto extends NotificationDto {

    private Integer communityNumber;

    public CommunityCommentNotificationDto(CommunityCommentNotification notification) {
        super(notification);
        this.communityNumber = notification.getCommunityNumber();
    }
}
