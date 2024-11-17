package swyp.swyp6_team7.travel.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelCreateRequest {

    private String locationName;
    @Size(max = 20)
    private String title;
    private String details;
    @PositiveOrZero
    private int maxPerson;
    private String genderType;
    @FutureOrPresent
    private LocalDate dueDate;
    private String periodType;
    @NotNull
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    @NotNull
    private boolean completionStatus;


    public TravelCreateRequest(
            String locationName, String title, String details,
            int maxPerson, String genderType, LocalDate dueDate, String periodType,
            List<String> tags, boolean completionStatus
    ) {
        this.locationName = locationName;
        this.title = title;
        this.details = details;
        this.maxPerson = maxPerson;
        this.genderType = genderType;
        this.dueDate = dueDate;
        this.periodType = periodType;
        this.tags = tags;
        this.completionStatus = completionStatus;
    }

}
