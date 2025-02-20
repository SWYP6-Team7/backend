package swyp.swyp6_team7.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.dto.PasswordChangeRequest;
import swyp.swyp6_team7.auth.dto.PasswordVerifyRequest;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.PasswordChangeService;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/password")
public class PasswordController {

    private final PasswordChangeService passwordChangeService;
    private final JwtProvider jwtProvider;

    // 현재 비밀번호 확인
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCurrentPassword(
            @RequireUserNumber Integer userNumber,
            @RequestBody PasswordVerifyRequest passwordVerifyRequest
    ) {
        try {
            log.info("현재 비밀번호 확인 요청");
            if (passwordVerifyRequest.getConfirmPassword() == null) {
                log.warn("요청에 비밀번호 확인이 누락되었습니다");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("현재 비밀번호가 입력되지 않았습니다.");
            }
            log.info("토큰에서 userNumber 추출 - userNumber: {}", userNumber);

            passwordChangeService.verifyCurrentPassword(userNumber, passwordVerifyRequest.getConfirmPassword());

            log.info("현재 비밀번호 확인 성공 - userNumber: {}", userNumber);
            return ResponseEntity.ok("현재 비밀번호와 일치합니다.");
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("현재 비밀번호 확인 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("현재 비밀번호 확인 중 에러가 발생했습니다.");
        }
    }

    // 새 비밀번호 설정
    @PutMapping("/change")
    public ResponseEntity<String> changePassword(
            @RequireUserNumber Integer userNumber,
            @RequestBody PasswordChangeRequest passwordChangeRequest
    ) {
        try {
            log.info("새 비밀번호 설정 요청");

            // 입력값 검증
            if (passwordChangeRequest.getNewPassword() == null || passwordChangeRequest.getNewPassword().isEmpty()) {
                log.warn("요청에 새 비밀번호가 누락되었습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("새 비밀번호 값을 입력해주세요");
            }
            if (passwordChangeRequest.getNewPasswordConfirm() == null || passwordChangeRequest.getNewPasswordConfirm().isEmpty()) {
                log.warn("요청에 새 비밀번호 확인이 누락되었습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("새 비밀번호가 일치하지 않습니다.");
            }

            log.info("토큰에서 userNumber 추출 - userNumber: {}", userNumber);

            passwordChangeService.changePassword(userNumber, passwordChangeRequest.getNewPassword(), passwordChangeRequest.getNewPasswordConfirm());

            log.info("새 비밀번호 설정 완료 - userNumber: {}", userNumber);
            return ResponseEntity.ok("새 비밀번호 설정이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("새 비밀번호 설정 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("새 비밀번호 설정 중 에러가 발생했습니다.");
        }
    }
}

