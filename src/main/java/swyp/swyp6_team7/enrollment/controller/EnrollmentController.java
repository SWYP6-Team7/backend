package swyp.swyp6_team7.enrollment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.enrollment.dto.EnrollmentCreateRequest;
import swyp.swyp6_team7.enrollment.service.EnrollmentService;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
public class EnrollmentController {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);
    private final EnrollmentService enrollmentService;


    @PostMapping("/api/enrollment")
    public ResponseEntity create(@Valid @RequestBody EnrollmentCreateRequest request) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        logger.info("Enrollment 생성 요청 - userId: {}, travelNumber: {}", loginUserNumber, request.getTravelNumber());

        enrollmentService.create(request, loginUserNumber, LocalDate.now());
        logger.info("Enrollment 생성 완료 - userId: {}, travelNumber: {}", loginUserNumber, request.getTravelNumber());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("여행 참가 신청이 완료되었습니다.");
    }

    @DeleteMapping("/api/enrollment/{enrollmentNumber}")
    public ResponseEntity delete(@PathVariable(name = "enrollmentNumber") long enrollmentNumber) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        logger.info("Enrollment 취소 요청 - userId: {}, travelNumber: {}", loginUserNumber, enrollmentNumber);

        enrollmentService.delete(enrollmentNumber, loginUserNumber);
        logger.info("Enrollment 취소 완료 - userId: {}, travelNumber: {}", loginUserNumber, enrollmentNumber);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("여행 참가 신청이 취소되었습니다.");
    }

    @PutMapping("/api/enrollment/{enrollmentNumber}/acceptance")
    public ResponseEntity accept(@PathVariable(name = "enrollmentNumber") long enrollmentNumber) {
        enrollmentService.accept(enrollmentNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body("신청을 수락했습니다.");
    }

    @PutMapping("/api/enrollment/{enrollmentNumber}/rejection")
    public ResponseEntity reject(@PathVariable(name = "enrollmentNumber") long enrollmentNumber) {
        enrollmentService.reject(enrollmentNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body("신청을 거부했습니다.");
    }

}
