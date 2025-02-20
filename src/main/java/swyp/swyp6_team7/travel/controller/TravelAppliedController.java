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

@RestController
@RequestMapping("/api/my-applied-travels")
@RequiredArgsConstructor
public class TravelAppliedController {

    private final TravelAppliedService travelAppliedService;

    // 사용자가 신청한 여행 목록 조회
    @GetMapping("")
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
    @DeleteMapping("/{travelNumber}/cancel")
    public ApiResponse<Void> cancelTripApplication(
            @PathVariable("travelNumber") int travelNumber,
            @RequireUserNumber Integer userNumber
    ) {

        // 참가 취소 처리
        travelAppliedService.cancelApplication(userNumber, travelNumber);
        return ApiResponse.success(null);
    }
}
