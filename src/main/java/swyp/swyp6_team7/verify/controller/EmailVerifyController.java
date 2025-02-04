package swyp.swyp6_team7.verify.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.verify.dto.request.EmailVerifyCheckRequest;
import swyp.swyp6_team7.verify.dto.request.EmailVerifySendRequest;
import swyp.swyp6_team7.verify.dto.response.EmailVerifySendResponse;
import swyp.swyp6_team7.verify.service.EmailVerifyService;

@RestController
@Slf4j
@RequestMapping("/api/verify/email")
public class EmailVerifyController {

    private final EmailVerifyService emailVerifyService;

    public EmailVerifyController(EmailVerifyService emailVerifyService) {
        this.emailVerifyService = emailVerifyService;
    }

    @PostMapping("/send")
    public ApiResponse<EmailVerifySendResponse> sendEmailVerificationCode(
            @RequestBody EmailVerifySendRequest request
    ) {
        String sessionToken = emailVerifyService.sendEmailVerification(request.getEmail());
        EmailVerifySendResponse response = new EmailVerifySendResponse(sessionToken, request.getEmail());
        return ApiResponse.success(response);
    }

    @PostMapping("")
    public ApiResponse<Boolean> verifyEmail(
            @RequestBody EmailVerifyCheckRequest request
    ) {
        final String code = request.getVerifyCode();
        final String sessionToken = request.getSessionToken();
        final boolean result = emailVerifyService.verifyEmailCode(sessionToken, code);
        return ApiResponse.success(result);
    }
}
