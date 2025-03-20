package swyp.swyp6_team7.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    // Access Token을 블랙리스트에 추가
    public void addToBlacklist(String refreshToken, long expirationTime) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(refreshToken, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
            log.info("Refresh Token 블랙리스트에 추가: token={}, expirationTime={}", refreshToken, expirationTime);
        } catch (Exception e) {
            log.error("Access Token을 블랙리스트에 추가하는 중 오류 발생: token={}, expirationTime={}", refreshToken, expirationTime, e);
            throw new RuntimeException("Failed to add token to blacklist", e);
        }
    }

    // Access Token이 블랙리스트에 있는지 확인
    public boolean isTokenBlacklisted(String refreshToken) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            boolean isBlacklisted = "blacklisted".equals(valueOperations.get(refreshToken));
            log.info("Refresh Token 블랙리스트 확인: token={}, isBlacklisted={}", refreshToken, isBlacklisted);
            return isBlacklisted;
        } catch (Exception e) {
            log.error("블랙리스트에서 Refresh Token 확인 중 오류 발생: token={}", refreshToken, e);
            throw new RuntimeException("Failed to check if token is blacklisted", e);
        }
    }
}
