package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelRequestedService;

@RestController
@RequestMapping("/api/my-requested-travels")
@RequiredArgsConstructor
public class TravelRequestedController {
    private final TravelRequestedService travelRequestedService;

    //신청한 여행 목록 조회
    @GetMapping("")
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
    @DeleteMapping("/{travelNumber}/cancel")
    public ApiResponse<Void> cancelTripApplication(
            @RequireUserNumber Integer userNumber,
            @PathVariable("travelNumber") int travelNumber
    ) {
        // 참가 취소 처리
        travelRequestedService.cancelApplication(userNumber, travelNumber);
        return ApiResponse.success(null);
    }
}
