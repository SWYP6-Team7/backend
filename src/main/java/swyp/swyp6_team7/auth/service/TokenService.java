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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private static final String REFRESH_TOKEN_LOCK_PREFIX = "refreshTokenLock:";
    private static final String REFRESH_TOKEN_CACHE_PREFIX = "refreshTokenCache:";
    private static final String REFRESH_TOKEN_STORE_PREFIX = "refreshToken:";

    private final JwtProvider jwtProvider;
    @Qualifier("redisTemplate")
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    public long getRefreshTokenValidity(){
        return 7*24*60*60; //초단위, 7일
    }

    //로그인 시 생성된 RefreshToken 저장
    public void storeRefreshToken(Integer userNumber, String refreshToken){
        String key = REFRESH_TOKEN_STORE_PREFIX + userNumber;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(getRefreshTokenValidity()));
    }

    //로그아웃 또는 토큰 교체 시 저장된 RefreshToken 삭제
    public void deleteRefreshToken(Integer userNumber){
        String key = REFRESH_TOKEN_STORE_PREFIX + userNumber;
        redisTemplate.delete(key);
    }

    // 분산 락을 사용해 Refresh Token으로 새로운 토큰 발급 (Token Rotation)
    public Map<String, String> refreshWithLock(String providedRefreshToken) {
        log.info("refreshWithLock 메서드 호출: refreshToken={}",providedRefreshToken);

        // JWT 검증 및 Refresh Token 디코딩
        if(!jwtProvider.validateToken(providedRefreshToken)){
            log.error("유효하지 않는 RefreshToken: {}", providedRefreshToken);
            throw new JwtException("유효하지 않은 RefreshToken입니다.");
        }
        Integer userNumber = jwtProvider.getUserNumber(providedRefreshToken);

        // Redis에 저장된 Refresh Token과 일치하는지 확인
        String storedRefreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_STORE_PREFIX+userNumber);
        if(storedRefreshToken == null || !storedRefreshToken.equals(providedRefreshToken)){
            throw new JwtException("저장된 Refresh Token과 일치하지 않습니다.");
        }

        String lockKey = REFRESH_TOKEN_LOCK_PREFIX + providedRefreshToken;
        String cacheKey = REFRESH_TOKEN_CACHE_PREFIX + providedRefreshToken;

        // 캐시된 Access Token이 있으면 재사용
        String cachedAccessToken = redisTemplate.opsForValue().get(cacheKey);
        if (cachedAccessToken != null) {
            log.info("Redis 캐시에서 AccessToken 반환: {}", cachedAccessToken);
            return Map.of("accessToken",cachedAccessToken,"refreshToken",providedRefreshToken);
        }

        // 분산 락 획득 (동시 요청 방지)
        boolean lockAcquired = acquireLock(lockKey);
        if(!lockAcquired){
            log.error("Lock 획득 실패: refreshToken={}", providedRefreshToken);
            throw new IllegalStateException("동시 요청으로 인해 처리 중단: " + providedRefreshToken);
        }

        try{
            // 사용자 조회
            Users user = userRepository.findByUserNumber(userNumber)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음: userNumber={}", userNumber);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다. userNumber: " + userNumber);
                    });

            // 새로운 Access Token과 Refresh Token(회전) 발급
            String newAccessToken = jwtProvider.createAccessToken(userNumber, List.of("ROLE_USER"));
            String newRefreshToken = jwtProvider.createRefreshToken(userNumber);

            // Redis에 새 Refresh Token 저장(이전 토큰은 폐기)
            storeRefreshToken(userNumber,newRefreshToken);

            // 새 Access Token을 캐시 처리
            long remainingValidity = jwtProvider.getRemainingValidity(newAccessToken);
            redisTemplate.opsForValue().set(cacheKey, newAccessToken, Duration.ofSeconds(remainingValidity));
            log.info("Redis에 새 Access Token  캐싱 완료: cacheKey={}, TTL={}초", cacheKey, remainingValidity);


            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);
            return tokens;
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
