package swyp.swyp6_team7.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import swyp.swyp6_team7.community.domain.Community;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue("community_comment")
public class CommunityCommentNotification extends Notification {

    @Column(name = "community_number")
    private Integer communityNumber;

    @Column(name = "notification_count")
    private Integer notificationCount;

    public CommunityCommentNotification(
            Long number, LocalDateTime createdAt, Integer receiverNumber,
            String title, String content, Boolean isRead,
            Integer communityNumber, Integer notificationCount
    ) {
        super(number, createdAt, receiverNumber, title, content, isRead);
        this.communityNumber = communityNumber;
        this.notificationCount = notificationCount;
    }

    public static CommunityCommentNotification create(Community targetPost, Integer notificationCount){
        return CommunityCommentNotification.builder()
                .receiverNumber(targetPost.getUserNumber())
                .title("커뮤니티")
                .content(String.format("[%s]에 댓글이 %d개 달렸어요.", targetPost.getTitle(), notificationCount))
                .isRead(false)
                .communityNumber(targetPost.getPostNumber())
                .notificationCount(notificationCount)
                .build();
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
                ", communityNumber=" + communityNumber +
                ", notificationCount=" + notificationCount +
                '}';
    }
}
