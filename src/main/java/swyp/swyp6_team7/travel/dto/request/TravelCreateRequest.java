package swyp.swyp6_team7.travel.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import swyp.swyp6_team7.plan.dto.request.PlanCreateRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelCreateRequest {

    private String locationName; // 여행지 이름
    private String countryName; // 여행지 국가 이름
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
    @Valid
    @Size(max = 90, message = "일정은 90개를 초과할 수 없습니다.")
    @Builder.Default
    private List<PlanCreateRequest> plans = new ArrayList<>(); // 여행 일정 정보

}
