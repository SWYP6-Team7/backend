package swyp.swyp6_team7.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import swyp.swyp6_team7.notification.entity.TravelNotification;

import java.time.LocalDate;

@Getter
public class TravelNotificationDto extends NotificationDto {

    private Integer travelNumber;

    private String travelTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate travelDueDate;

    private Boolean travelHostUser;


    public TravelNotificationDto(TravelNotification notification) {
        super(notification);
        this.travelNumber = notification.getTravelNumber();
        this.travelTitle = notification.getTravelTitle();
        this.travelDueDate = notification.getTravelDueDate();
        this.travelHostUser = notification.getTravelHost();
    }

}
