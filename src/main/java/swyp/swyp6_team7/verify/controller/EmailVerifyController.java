package swyp.swyp6_team7.verify.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<?> sendEmailVerificationCode(
            @RequestParam String email
    ) {
        emailVerifyService.sendVerifyEmail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(
            @RequestParam String email,
            @RequestParam String code
    ) {
        final boolean result = emailVerifyService.verifyEmailCode(email, code);
        return ResponseEntity.ok().body(result);
    }
}
