package swyp.swyp6_team7.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationMessageType {

    TRAVEL_ENROLL_HOST("여행 신청 알림") {
        public String getContent(String travelTitle) {
            return String.format("[%s]에 참가 신청자가 있어요. 알림을 눌러 확인해보세요.", travelTitle);
        }
    },
    TRAVEL_ENROLL("참가 신청 알림") {
        public String getContent(String travelTitle) {
            return String.format("[%s]에 참가 신청이 완료되었어요. 주최자가 참가를 확정하면 알려드릴게요.", travelTitle);
        }
    },

    TRAVEL_ACCEPT("참가 확정 알림") {
        public String getContent(String travelTitle) {
            return String.format("[%s]에 참가가 확정되었어요. 멤버 댓글을 통해 인사를 나눠보세요.", travelTitle);
        }
    },

    TRAVEL_REJECT("참가 거절 알림") {
        public String getContent(String travelTitle) {
            return String.format("[%s]에 참가가 아쉽게도 거절되었어요. 다른 여행을 찾아볼까요?", travelTitle);
        }
    },

    TRAVEL_NEW_COMMENT_HOST("멤버 댓글 알림") {
        public String getContent(String travelTitle) {
            return String.format("개설하신 [%s]에 멤버 댓글이 달렸어요. 확인해보세요.", travelTitle);
        }
    },

    TRAVEL_COMPANION_CLOSED_HOST("모집 마감 알림") {
        public String getContent(String travelTitle) {
            return String.format("[%s]의 인원이 가득 차 모집이 마감되었어요.", travelTitle);
        }
    },

    TRAVEL_CLOSED_COMPANION("모집 마감 알림") {
        public String getContent(String travelTitle) {
            return String.format("참가하신 [%s]의 모집이 마감되었어요.", travelTitle);
        }
    },

    TRAVEL_CLOSED_PENDING("모집 마감 알림") {
        public String getContent(String travelTitle) {
            return String.format("참가 신청하신 [%s]의 모집이 마감되었어요.", travelTitle);
        }
    },

    TRAVEL_CLOSED_BOOKMARKED("모집 마감 알림") {
        public String getContent(String travelTitle) {
            return String.format("즐겨찾기하신 [%s]의 모집이 마감되었어요.", travelTitle);
        }
    },

    TRAVEL_NEW_COMMENT_COMPANION("멤버 댓글 알림") {
        public String getContent(String travelTitle) {
            return String.format("참가 신청하신 [%s]에 멤버 댓글이 달렸어요. 확인해보세요.", travelTitle);
        }
    };

    private final String title;

    public abstract String getContent(String travelTitle);
}
