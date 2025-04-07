package swyp.swyp6_team7.enrollment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.enrollment.dto.EnrollmentCreateRequest;
import swyp.swyp6_team7.enrollment.service.EnrollmentService;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/api/enrollment")
    public ApiResponse<String> create(
            @Valid @RequestBody EnrollmentCreateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        enrollmentService.create(request, userNumber, LocalDate.now());
        return ApiResponse.success("여행 참가 신청이 완료되었습니다.");
    }

    @DeleteMapping("/api/enrollment/{enrollmentNumber}")
    public ApiResponse<String> delete(
            @PathVariable(name = "enrollmentNumber") long enrollmentNumber,
            @RequireUserNumber Integer userNumber
    ) {
        enrollmentService.delete(enrollmentNumber, userNumber);
        return ApiResponse.success("여행 참가 신청이 취소되었습니다.");
    }

    @PutMapping("/api/enrollment/{enrollmentNumber}/acceptance")
    public ApiResponse<String> accept(
            @PathVariable(name = "enrollmentNumber") long enrollmentNumber,
            @RequireUserNumber Integer userNumber
    ) {
        enrollmentService.accept(enrollmentNumber, userNumber);
        return ApiResponse.success("여행 참가 신청을 수락했습니다.");
    }

    @PutMapping("/api/enrollment/{enrollmentNumber}/rejection")
    public ApiResponse<String> reject(
            @PathVariable(name = "enrollmentNumber") long enrollmentNumber,
            @RequireUserNumber Integer userNumber
    ) {
        enrollmentService.reject(enrollmentNumber, userNumber);
        return ApiResponse.success("여행 참가 신청을 거절했습니다.");
    }
}