package swyp.swyp6_team7.travel.dto.request;

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

    private String locationName; // 여행지 이름
    @NotNull
    private LocalDate startDate; // 여행 시작 일자
    @NotNull
    private LocalDate endDate; // 여행 종료 일자
    @Size(max = 20, message = "여행 제목은 최대 20자 입니다.")
    private String title; // 여행 게시글 제목
    private String details; // 여행 게시글 본문
    @PositiveOrZero(message = "여행 참가 최대 인원은 0보다 작을 수 없습니다.")
    private int maxPerson;
    private String genderType; // 모집 성별 타입
    private String periodType; // 여행 기간 타입
    @NotNull
    @Builder.Default
    private List<String> tags = new ArrayList<>(); // 게시글 태그

    public TravelCreateRequest(
            String locationName, LocalDate startDate, LocalDate endDate, String title, String details,
            int maxPerson, String genderType, String periodType, List<String> tags
    ) {
        this.locationName = locationName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.details = details;
        this.maxPerson = maxPerson;
        this.genderType = genderType;
        this.periodType = periodType;
        this.tags = tags;
    }

}
