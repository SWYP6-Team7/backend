package swyp.swyp6_team7.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.dto.PasswordChangeRequest;
import swyp.swyp6_team7.auth.dto.PasswordVerifyRequest;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.PasswordChangeService;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
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
    public ApiResponse<String> verifyCurrentPassword(
            @RequireUserNumber Integer userNumber,
            @RequestBody PasswordVerifyRequest passwordVerifyRequest
    ) {
        try {
            log.info("현재 비밀번호 확인 요청");
            if (passwordVerifyRequest.getConfirmPassword() == null) {
                log.warn("요청에 비밀번호 확인이 누락되었습니다");
                throw new MoingApplicationException("현재 비밀번호가 입력되지 않았습니다.");
            }
            log.info("토큰에서 userNumber 추출 - userNumber: {}", userNumber);

            passwordChangeService.verifyCurrentPassword(userNumber, passwordVerifyRequest.getConfirmPassword());

            log.info("현재 비밀번호 확인 성공 - userNumber: {}", userNumber);
            return ApiResponse.success("현재 비밀번호와 일치합니다.");
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("현재 비밀번호 확인 중 에러 발생", e);
            throw e;
        }
    }

    // 새 비밀번호 설정
    @PutMapping("/change")
    public ApiResponse<String> changePassword(
            @RequireUserNumber Integer userNumber,
            @RequestBody PasswordChangeRequest passwordChangeRequest
    ) {
        try {
            log.info("새 비밀번호 설정 요청");

            // 입력값 검증
            if (passwordChangeRequest.getNewPassword() == null || passwordChangeRequest.getNewPassword().isEmpty()) {
                log.warn("요청에 새 비밀번호가 누락되었습니다.");
                throw new MoingApplicationException("새 비밀번호 값을 입력해주세요");
            }
            if (passwordChangeRequest.getNewPasswordConfirm() == null || passwordChangeRequest.getNewPasswordConfirm().isEmpty()) {
                log.warn("요청에 새 비밀번호 확인이 누락되었습니다.");
                throw new MoingApplicationException("새 비밀번호가 일치하지 않습니다.");
            }

            log.info("토큰에서 userNumber 추출 - userNumber: {}", userNumber);

            passwordChangeService.changePassword(userNumber, passwordChangeRequest.getNewPassword(), passwordChangeRequest.getNewPasswordConfirm());

            log.info("새 비밀번호 설정 완료 - userNumber: {}", userNumber);
            return ApiResponse.success("새 비밀번호 설정이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("새 비밀번호 설정 중 에러 발생", e);
            throw e;
        }
    }
}

