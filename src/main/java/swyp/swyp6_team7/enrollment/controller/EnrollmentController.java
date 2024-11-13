package swyp.swyp6_team7.enrollment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.enrollment.dto.EnrollmentCreateRequest;
import swyp.swyp6_team7.enrollment.service.EnrollmentService;
import swyp.swyp6_team7.member.service.MemberService;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;


    @PostMapping("/api/enrollment")
    public ResponseEntity create(
            @RequestBody @Validated EnrollmentCreateRequest request, Principal principal
    ) {
        Integer userNumber = memberService.findByUserNumber(
                jwtProvider.getUserNumber(principal.getName())
        ).getUserNumber();
        enrollmentService.create(request, userNumber);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("여행 참가 신청이 완료되었습니다.");
    }

    @DeleteMapping("/api/enrollment/{enrollmentNumber}")
    public ResponseEntity delete(@PathVariable(name = "enrollmentNumber") long enrollmentNumber) {
        enrollmentService.delete(enrollmentNumber);
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
