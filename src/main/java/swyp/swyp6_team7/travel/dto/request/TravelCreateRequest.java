package swyp.swyp6_team7.travel.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelCreateRequest {

    private String locationName;
    // todo: 시작, 종료 날짜 필드 추가
    @Size(max = 20, message = "여행 제목은 최대 20자 입니다.")
    private String title;
    private String details;
    @PositiveOrZero(message = "여행 참가 최대 인원은 0보다 작을 수 없습니다.")
    private int maxPerson;
    private String genderType;
    private String periodType;
    @NotNull
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    @NotNull
    private boolean completionStatus;


    public TravelCreateRequest(
            String locationName, String title, String details,
            int maxPerson, String genderType, String periodType,
            List<String> tags, boolean completionStatus
    ) {
        this.locationName = locationName;
        this.title = title;
        this.details = details;
        this.maxPerson = maxPerson;
        this.genderType = genderType;
        this.periodType = periodType;
        this.tags = tags;
        this.completionStatus = completionStatus;
    }

}
