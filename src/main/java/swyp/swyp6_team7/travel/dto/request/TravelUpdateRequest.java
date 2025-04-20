package swyp.swyp6_team7.travel.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import swyp.swyp6_team7.plan.dto.request.AllPlanUpdateRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelUpdateRequest {

    private String locationName;
    private String countryName;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
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
    @Valid
    @Builder.Default
    private AllPlanUpdateRequest planChanges = new AllPlanUpdateRequest();
}
