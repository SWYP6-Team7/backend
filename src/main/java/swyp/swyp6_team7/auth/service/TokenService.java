package swyp.swyp6_team7.auth.service;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private static final String REFRESH_TOKEN_LOCK_PREFIX = "refreshTokenLock:";

    private final JwtProvider jwtProvider;
    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    public String refreshWithLock(String refreshToken) {
        log.info("refreshWithLock 메서드 호출: refreshToken={}",refreshToken);

        String lockKey = REFRESH_TOKEN_LOCK_PREFIX + refreshToken;

        boolean lockAcquired = acquireLock(lockKey);
        if(!lockAcquired){
            log.error("Lock 획득 실패: refreshToken={}", refreshToken);
            throw new IllegalStateException("동시 요청으로 인해 처리 중단: " + refreshToken);
        }

        try{
            log.info("Lock 획득 성공: lockKey={}", lockKey);
            if(!jwtProvider.validateToken(refreshToken)){
                log.error("유효하지 않는 RefreshToken: {}", refreshToken);
                throw new JwtException("유효하지 않은 RefreshToken입니다.");
            }

            Integer userNumber = jwtProvider.getUserNumber(refreshToken);
            log.info("토큰에서 추출한 userNumber={}",userNumber);
            Users user = userRepository.findByUserNumber(userNumber)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음: userNumber={}", userNumber);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다. userNumber: " + userNumber);
                    });
            log.info("사용자 확인 완료: userNumber={}", userNumber);
            String newAccessToken = jwtProvider.createAccessToken(userNumber, List.of("ROLE_USER"));
            log.info("새로운 AccessToken 생성 완료: userNumber={}", userNumber);

            return newAccessToken;
        } catch (JwtException e){
            log.error("JWT 처리 중 예외 발생: {}", e.getMessage());
            throw e;
        } catch (ResponseStatusException e) {
            log.error("사용자 조회 중 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new IllegalStateException("예기치 못한 오류가 발생했습니다.", e);
        }
        finally{
            releaseLock(lockKey);
            log.info("Lock 해제 완료: lockKey={}", lockKey);
        }
    }
    private boolean acquireLock(String lockKey){
        log.info("Lock 시도: lockKey={}", lockKey);
        boolean result = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(5)));
        if (result) {
            log.info("Lock 성공: lockKey={}", lockKey);
        } else {
            log.warn("Lock 실패: lockKey={}", lockKey);
        }
        return result;
    }

    private void releaseLock(String lockKey){
        log.info("Lock 해제 시도: lockKey={}", lockKey);
        redisTemplate.delete(lockKey);
        log.info("Lock 해제 완료: lockKey={}", lockKey);

    }
}
