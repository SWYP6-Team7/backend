package swyp.swyp6_team7.auth.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public JwtBlacklistService(@Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Access Token을 블랙리스트에 추가
    public void addToBlacklist(String token, long expirationTime) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(token, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }

    // Access Token이 블랙리스트에 있는지 확인
    public boolean isTokenBlacklisted(String token) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return "blacklisted".equals(valueOperations.get(token));
    }
}
