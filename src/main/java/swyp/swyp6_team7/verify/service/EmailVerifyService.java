package swyp.swyp6_team7.verify.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.global.email.EmailSenderProcessor;
import swyp.swyp6_team7.global.email.template.EmailVerificationCodeMessage;

import java.time.Duration;
import java.util.Objects;

@AllArgsConstructor
@Service
@Slf4j
public class EmailVerifyService {

    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final EmailSenderProcessor emailSenderProcessor;

    private static final String emailVerifyKey = "email-verification:";

    @Transactional
    public void sendVerifyEmail(String email) {
        // TODO: Redis 기반으로 변경
        final String code = RandomStringUtils.randomNumeric(6); // 6자리 인증번호

        EmailVerificationCodeMessage message = new EmailVerificationCodeMessage(
                email,
                code,
                "이메일 인증 안내",
                "회원 이메일 인증을 위한 인증번호입니다"
        );
        emailSenderProcessor.process(message);

        jsonRedisTemplate.opsForValue().set(emailVerifyKey.concat(email), code, Duration.ofMinutes(20));

        log.info("Email Verify code send. email: {}", email);
    }

    @Transactional
    public boolean verifyEmailCode(String email, String verifyCode) {
        final Object originCode = jsonRedisTemplate.opsForValue().get(emailVerifyKey.concat(email));
        if (originCode == null) {
            log.info("Email Verify code not found. email: {}", email);
            return false;
        }

        final String code = Objects.requireNonNull(originCode).toString();

        return code.equals(verifyCode);
    }
}
