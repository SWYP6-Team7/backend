package swyp.swyp6_team7.enrollment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Users;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentResponse {

    private long enrollmentNumber;
    private String userName;
    private String userAgeGroup;
    private String profileUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime enrolledAt;
    private String message;
    private String status;

    @Builder
    @QueryProjection
    public EnrollmentResponse(
            long enrollmentNumber, String userName, AgeGroup ageGroup, String profileUrl,
            LocalDateTime enrolledAt, String message, EnrollmentStatus status
    ) {
        this.enrollmentNumber = enrollmentNumber;
        this.userName = userName;
        this.userAgeGroup = ageGroup.getValue();
        this.profileUrl = profileUrl;
        this.enrolledAt = enrolledAt;
        this.message = message;
        this.status = status.toString();
    }

}
