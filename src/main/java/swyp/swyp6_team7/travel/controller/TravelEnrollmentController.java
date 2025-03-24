package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.enrollment.service.EnrollmentService;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.travel.dto.request.TravelEnrollmentLastViewedRequest;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentLastViewedResponse;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentsResponse;
import swyp.swyp6_team7.travel.service.TravelService;

@RequiredArgsConstructor
@RestController
public class TravelEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final TravelService travelService;

    // 여행 신청 목록 조회
    @GetMapping("/api/travel/{travelNumber}/enrollments")
    public ApiResponse<TravelEnrollmentsResponse> findEnrollments(
            @PathVariable("travelNumber") int travelNumber,
            @RequireUserNumber Integer userNumber
    ) {
        TravelEnrollmentsResponse response = enrollmentService.findEnrollmentsByTravelNumber(travelNumber, userNumber);
        return ApiResponse.success(response);
    }

    // 여행 신청 목록을 마지막으로 확인한 시각 조회
    @GetMapping("/api/travel/{travelNumber}/enrollments/last-viewed")
    public ApiResponse<TravelEnrollmentLastViewedResponse> getEnrollmentsLastViewedTime(@PathVariable("travelNumber") int travelNumber) {
        TravelEnrollmentLastViewedResponse response = new TravelEnrollmentLastViewedResponse(
                travelService.getEnrollmentsLastViewedAt(travelNumber)
        );
        return ApiResponse.success(response);
    }

    // 여행 신청 목록을 마지막으로 확인한 시각 갱신
    @PutMapping("/api/travel/{travelNumber}/enrollments/last-viewed")
    public ApiResponse<String> updateEnrollmentsLastViewedTime(
            @PathVariable("travelNumber") int travelNumber,
            @RequestBody TravelEnrollmentLastViewedRequest request
    ) {
        travelService.updateEnrollmentLastViewedAt(travelNumber, request.getLastViewedAt());
        return ApiResponse.success("신청 목록 LastViewedAt 수정 완료");
    }

    // 여행 신청 개수 조회
    @GetMapping("/api/travel/{travelNumber}/enrollmentCount")
    public ApiResponse<Long> getEnrollmentsCount(@PathVariable("travelNumber") int travelNumber) {
        return ApiResponse.success(enrollmentService.getPendingEnrollmentsCountByTravelNumber(travelNumber));
    }
}
