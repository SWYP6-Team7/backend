package swyp.swyp6_team7.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentCreateRequest {

    @NotNull(message = "여행 참가 신청 시 travelNumber는 필수값입니다.")
    private Integer travelNumber;

    @Size(max = 1000, message = "여행 참가 신청 메시지는 1000자를 넘을 수 없습니다.")
    private String message;

    @Builder
    public EnrollmentCreateRequest(Integer travelNumber, String message) {
        this.travelNumber = travelNumber;
        this.message = message;
    }

}
