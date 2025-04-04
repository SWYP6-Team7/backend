package swyp.swyp6_team7.travel.dto;

import lombok.Builder;
import lombok.Getter;
import swyp.swyp6_team7.enrollment.domain.Enrollment;

@Getter
public class TravelDetailLoginMemberRelatedDto {

    private boolean hostUser;       // 주최자 여부
    private Long enrollmentNumber;       // 신청 번호(없으면 null)
    private boolean bookmarked;     // 북마크 여부

    @Builder
    public TravelDetailLoginMemberRelatedDto(boolean isHostUser, Long enrollmentNumber, boolean isBookmarked) {
        this.hostUser = isHostUser;
        this.enrollmentNumber = enrollmentNumber;
        this.bookmarked = isBookmarked;
    }

    public TravelDetailLoginMemberRelatedDto() {
        this.hostUser = false;
        this.enrollmentNumber = null;
        this.bookmarked = false;
    }

    public void setHostUserCheckTrue() {
        this.hostUser = true;
    }

    public void setEnrollmentNumber(Long enrollmentNumber) {
        if (enrollmentNumber != null) {
            this.enrollmentNumber = enrollmentNumber;
        }
    }

    public void setBookmarkedTrue() {
        this.bookmarked = true;
    }

    @Override
    public String toString() {
        return "TravelDetailLoginMemberDto{" +
                "hostUser=" + hostUser +
                ", enrollmentNumber=" + enrollmentNumber +
                ", bookmarked=" + bookmarked +
                '}';
    }
}
