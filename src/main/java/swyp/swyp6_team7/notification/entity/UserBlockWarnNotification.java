package swyp.swyp6_team7.notification.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder // 부모클래스 Builder 가능
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue("user_block_warn")
public class UserBlockWarnNotification extends Notification {

    public static UserBlockWarnNotification create(int receiverNumber, int reportCount) {
        return UserBlockWarnNotification.builder()
                .receiverNumber(receiverNumber)
                .title("신고 접수 경고")
                .content(String.format("신고가 %d회 접수되었습니다. 신고에 대한 사유를 소명하지 않으면 회원님의 프로필에 신고 내용이 노출되며, 이에 따른 불이익이 존재할 수 있습니다.", reportCount))
                .isRead(false)
                .build();
    }

    @Override
    public String toString() {
        return "UserBlockWarnNotification{" +
                "number=" + super.getNumber() +
                ", createdAt=" + super.getCreatedAt() +
                ", receiverNumber=" + super.getReceiverNumber() +
                ", title='" + super.getTitle() + '\'' +
                ", content='" + super.getContent() + '\'' +
                ", isRead=" + super.getIsRead() +
                '}';
    }
}
