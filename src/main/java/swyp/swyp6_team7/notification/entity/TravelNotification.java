package swyp.swyp6_team7.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue("travel")
public class TravelNotification extends Notification {

    @Column(name = "travel_number")
    private Integer travelNumber;

    @Column(name = "travel_title", length = 20)
    private String travelTitle;

    @Column(name = "travel_host")
    private Boolean travelHost;


    public TravelNotification(
            Long number, LocalDateTime createdAt, Integer receiverNumber,
            String title, String content, Boolean isRead,
            Integer travelNumber, String travelTitle, Boolean travelHost
    ) {
        super(number, createdAt, receiverNumber, title, content, isRead);
        this.travelNumber = travelNumber;
        this.travelTitle = travelTitle;
        this.travelHost = travelHost;
    }

    @Override
    public String toString() {
        return "TravelNotification{" +
                "number=" + super.getNumber() +
                ", createdAt=" + super.getCreatedAt() +
                ", receiverNumber=" + super.getReceiverNumber() +
                ", title='" + super.getTitle() + '\'' +
                ", content='" + super.getContent() + '\'' +
                ", isRead=" + super.getIsRead() +
                ", travelNumber=" + travelNumber +
                ", travelTitle='" + travelTitle + '\'' +
                ", travelHost=" + travelHost +
                '}';
    }
}
