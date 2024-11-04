package swyp.swyp6_team7.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JwtBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public JwtBlacklistService(@Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Access Token을 블랙리스트에 추가
    public void addToBlacklist(String token, long expirationTime) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(token, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
            log.info("Access Token 블랙리스트에 추가: token={}, expirationTime={}", token, expirationTime);
        } catch (Exception e) {
            log.error("Access Token을 블랙리스트에 추가하는 중 오류 발생: token={}, expirationTime={}", token, expirationTime, e);
            throw new RuntimeException("Failed to add token to blacklist", e);
        }
    }

    // Access Token이 블랙리스트에 있는지 확인
    public boolean isTokenBlacklisted(String token) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            boolean isBlacklisted = "blacklisted".equals(valueOperations.get(token));
            log.info("Access Token 블랙리스트 확인: token={}, isBlacklisted={}", token, isBlacklisted);
            return isBlacklisted;
        } catch (Exception e) {
            log.error("블랙리스트에서 Access Token 확인 중 오류 발생: token={}", token, e);
            throw new RuntimeException("Failed to check if token is blacklisted", e);
        }
    }
}
