package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelAppliedService;
import swyp.swyp6_team7.travel.service.TravelListService;
import swyp.swyp6_team7.travel.service.TravelRequestedService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TravelListController {
    private final TravelListService travelListService;
    private final TravelAppliedService travelAppliedService;
    private final TravelRequestedService travelRequestedService;

    // 만든 여행 목록 조회
    @GetMapping("/my-travels")
    public ApiResponse<Page<TravelListResponseDto>> getMyCreatedTravels(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TravelListResponseDto> travelList = travelListService.getTravelListByUser(userNumber, pageable);

        return ApiResponse.success(travelList);
    }

    // 동행이 맺어진 여행 리스트
    @GetMapping("/my-applied-travels")
    public ApiResponse<Page<TravelListResponseDto>> getAppliedTrips(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber

    ) {
        Pageable pageable = PageRequest.of(page, size);
        // 여행 목록 조회
        Page<TravelListResponseDto> appliedTrips = travelAppliedService.getAppliedTripsByUser(userNumber, pageable);

        return ApiResponse.success(appliedTrips);
    }

    // 사용자가 특정 여행에 대한 참가 취소
    @DeleteMapping("/my-applied-travels/{travelNumber}/cancel")
    public ApiResponse<Void> cancelTripApplication(
            @PathVariable("travelNumber") int travelNumber,
            @RequireUserNumber Integer userNumber
    ) {

        // 참가 취소 처리
        travelAppliedService.cancelApplication(userNumber, travelNumber);
        return ApiResponse.success(null);
    }

    //참가 신청한 여행 목록 조회(주최자 수락 대기중)
    @GetMapping("/my-requested-travels")
    public ApiResponse<Page<TravelListResponseDto>> getRequestedTrips(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber
    ) {
        Pageable pageable = PageRequest.of(page, size);
        // 서비스 호출하여 신청한 여행 목록 조회
        Page<TravelListResponseDto> travelList = travelRequestedService.getRequestedTripsByUser(userNumber, pageable);

        return ApiResponse.success(travelList);
    }

    // 참가 취소
    @DeleteMapping("/my-requested-travels/{travelNumber}/cancel")
    public ApiResponse<Void> cancelTripApplication(
            @RequireUserNumber Integer userNumber,
            @PathVariable("travelNumber") int travelNumber
    ) {
        // 참가 취소 처리
        travelRequestedService.cancelApplication(userNumber, travelNumber);
        return ApiResponse.success(null);
    }
}
