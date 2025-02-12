package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.enrollment.service.EnrollmentService;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.travel.dto.request.TravelEnrollmentLastViewedRequest;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentLastViewedResponse;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentsResponse;
import swyp.swyp6_team7.travel.service.TravelService;

@RequiredArgsConstructor
@RestController
public class TravelEnrollmentController {

    private static final Logger logger = LoggerFactory.getLogger(TravelEnrollmentController.class);

    private final EnrollmentService enrollmentService;
    private final TravelService travelService;


    @GetMapping("/api/travel/{travelNumber}/enrollments")
    public ResponseEntity<TravelEnrollmentsResponse> findEnrollments(
            @PathVariable("travelNumber") int travelNumber,
            @RequireUserNumber Integer userNumber
    ) {
        logger.info("Enrollments 조회 요청 - userId: {}, travelNumber: {}", userNumber, travelNumber);

        return ResponseEntity.status(HttpStatus.OK)
                .body(enrollmentService.findEnrollmentsByTravelNumber(travelNumber, userNumber));
    }

    @GetMapping("/api/travel/{travelNumber}/enrollments/last-viewed")
    public ResponseEntity<TravelEnrollmentLastViewedResponse> getEnrollmentsLastViewedTime(@PathVariable("travelNumber") int travelNumber) {
        TravelEnrollmentLastViewedResponse response = new TravelEnrollmentLastViewedResponse(
                travelService.getEnrollmentsLastViewedAt(travelNumber)
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/api/travel/{travelNumber}/enrollments/last-viewed")
    public ResponseEntity updateEnrollmentsLastViewedTime(
            @PathVariable("travelNumber") int travelNumber,
            @RequestBody TravelEnrollmentLastViewedRequest request
    ) {
        travelService.updateEnrollmentLastViewedAt(travelNumber, request.getLastViewedAt());
        return ResponseEntity.status(HttpStatus.OK)
                .body("신청 목록 LastViewedAt 수정 완료");
    }

    @GetMapping("/api/travel/{travelNumber}/enrollmentCount")
    public ResponseEntity getEnrollmentsCount(@PathVariable("travelNumber") int travelNumber) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(enrollmentService.getPendingEnrollmentsCountByTravelNumber(travelNumber));
    }
}
