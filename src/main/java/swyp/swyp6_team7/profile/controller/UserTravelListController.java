package swyp.swyp6_team7.profile.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.profile.dto.VisitedCountryLogResponse;
import swyp.swyp6_team7.profile.service.VisitedCountryLogService;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelAppliedService;
import swyp.swyp6_team7.travel.service.TravelListService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserTravelListController {
    private final TravelListService travelListService;
    private final TravelAppliedService travelAppliedService;
    private final VisitedCountryLogService visitedCountryLogService;

    // 상대방의 만든 여행 목록 조회
    @GetMapping("/{targetUserNumber}/created-travels")
    public ApiResponse<Page<TravelListResponseDto>> getTargetUserCreatedTravels(
            @PathVariable("targetUserNumber") Integer targetUserNumber,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<TravelListResponseDto> createdTravelList = travelListService.getTravelListByUser(targetUserNumber, pageable);

        return ApiResponse.success(createdTravelList);
    }

    // 상대방의 참가한 여행 목록 조회
    @GetMapping("/{targetUserNumber}/applied-travels")
    public ApiResponse<Page<TravelListResponseDto>> getTargetUserAppliedTravels(
            @PathVariable("targetUserNumber") Integer targetUserNumber,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<TravelListResponseDto> appliedTravelList = travelAppliedService.getAppliedTripsByUser(targetUserNumber, pageable);

        return ApiResponse.success(appliedTravelList);
    }

    // 상대방의 방문한 여행 로그 조회
    @GetMapping("/{targetUserNumber}/visited-countries")
    public ApiResponse<VisitedCountryLogResponse> getVisitedCountries(
            @PathVariable("targetUserNumber") Integer targetUserNumber,
            @RequireUserNumber Integer userNumber) {
        return ApiResponse.success(visitedCountryLogService.getVisitedCountriesByUser(targetUserNumber));
    }
}
