package swyp.swyp6_team7.verify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.global.email.EmailSenderProcessor;
import swyp.swyp6_team7.global.email.template.EmailVerificationCodeMessage;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.verify.dto.EmailVerifySession;

import java.time.Duration;
import java.util.UUID;

@AllArgsConstructor
@Service
@Slf4j
public class EmailVerifyService {

    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final EmailSenderProcessor emailSenderProcessor;
    private final UserRepository userRepository;

    @Autowired
    private final ObjectMapper objectMapper;

    private static final String emailVerifyKey = "email-verification:";
    private static final String sessionKey = "verify:session:";

    private String generateSessionToken() {
        return UUID.randomUUID().toString();
    }

    public String sendEmailVerification(String email) {
        // 이메일 중복 체크
        if (userRepository.findByUserEmail(email).isPresent()) {
            throw new MoingApplicationException("이미 사용 중인 이메일입니다.");
        }

        // 1. 1분 내에 같은 Email 로 중복발송 방지
        final boolean alreadySent = jsonRedisTemplate.opsForValue().get(emailVerifyKey.concat(email)) != null;
        if (alreadySent) {
            log.info("이미 발송된 이메일입니다. {}", email);
            throw new MoingApplicationException("이미 발송된 이메일입니다. 1분 뒤에 다시 시도해주세요.");
        }

        // 2. Session Token 발급
        final String sessionToken = generateSessionToken();

        // 3. 인증코드 6자리 발급
        final String code = RandomStringUtils.randomNumeric(6); // 6자리 인증번호
        log.info("Email code: {}", code);

        // 4. Session Token 을 Key 로 하는 Redis Session 저장 (w/ 인증코드)
        EmailVerifySession session = new EmailVerifySession(email, code, false);
        jsonRedisTemplate.opsForValue().set(sessionKey.concat(sessionToken), session, Duration.ofMinutes(20));

        // 4-1. Email 인증세션 저장
        jsonRedisTemplate.opsForValue().set(emailVerifyKey.concat(email), true, Duration.ofMinutes(1));

        // 5. Email 인증 발송
        EmailVerificationCodeMessage message = new EmailVerificationCodeMessage(
                email,
                code,
                "이메일 인증 안내",
                "회원 이메일 인증을 위한 인증번호입니다"
        );
        emailSenderProcessor.process(message);
        log.info("Email Verify code send. email: {}", email);

        return sessionToken;
    }

    public boolean verifyEmailCode(String sessionToken, String verifyCode) {
        final Object sessionObject = jsonRedisTemplate.opsForValue().get(sessionKey.concat(sessionToken));
        if (sessionObject == null) {
            log.info("Email Verify code not found.");
            throw new MoingApplicationException("이메일 인증 요청이 없습니다.");
        }

        try {
            EmailVerifySession session = objectMapper.convertValue(sessionObject, EmailVerifySession.class);

            final String code = session.getCode();
            session.setVerified(true);
            log.info("Session set verified: {}", session);
            jsonRedisTemplate.opsForValue().set(sessionKey.concat(sessionToken), session, Duration.ofMinutes(10));

            return code.equals(verifyCode);
        } catch (Exception e) {
            log.error("Session 변환 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public void checkEmailVerified(String sessionToken, String email) {
        final Object sessionObject = jsonRedisTemplate.opsForValue().get(sessionKey.concat(sessionToken));

        if (sessionObject == null) {
            log.info("Email Verify not found. {}", email);
            throw new MoingApplicationException("이메일 인증이 완료되지 않았습니다.");
        }

        try {
            EmailVerifySession session = objectMapper.convertValue(sessionObject, EmailVerifySession.class);

            final boolean verified = session.isVerified();
            if (!verified) {
                throw new MoingApplicationException("이메일 인증이 완료되지 않았습니다.");
            }
        } catch (Exception e) {
            log.error("Session 변환 중 오류 발생: {}", e.getMessage());
            throw new MoingApplicationException("이메일 인증 내역을 확인하는 중 오류가 발생했습니다.");
        }
    }
}