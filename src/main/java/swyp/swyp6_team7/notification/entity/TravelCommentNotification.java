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
@DiscriminatorValue("travel_comment")
public class TravelCommentNotification extends Notification {

    @Column(name = "travel_number")
    private Integer travelNumber;


    public TravelCommentNotification(
            Long number, LocalDateTime createdAt, Integer receiverNumber,
            String title, String content, Boolean isRead,
            Integer travelNumber
    ) {
        super(number, createdAt, receiverNumber, title, content, isRead);
        this.travelNumber = travelNumber;
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
                "travelNumber=" + travelNumber +
                '}';
    }


}
