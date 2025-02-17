package swyp.swyp6_team7.auth.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TokenServiceTest {
    @Autowired
    private TokenService tokenService;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    @MockBean
    private UserRepository userRepository;
    private ValueOperations<String,String> valueOperations;

    @BeforeEach
    void setup(){
        jwtProvider = mock(JwtProvider.class);
        redisTemplate = mock(RedisTemplate.class);
        userRepository = mock(UserRepository.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        tokenService = new TokenService(jwtProvider, redisTemplate, userRepository);
    }

    @Test
    @DisplayName("유효한 RefreshToken으로 새로운 AccessToken 생성")
    void refreshWithLock_success(){
        String refreshToken = "validRefreshToken";
        Integer userNumber = 12345;
        String lockKey = "refreshTokenLock:"+refreshToken;
        String accessCacheKey = "refreshTokenCache:"+refreshToken;
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        long ttl = 900;

        when(valueOperations.setIfAbsent(eq(lockKey), eq("LOCKED"),eq(Duration.ofSeconds(5)))).thenReturn(true);
        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getUserNumber(refreshToken)).thenReturn(userNumber);
        when(valueOperations.get("refreshToken:"+userNumber)).thenReturn(refreshToken);
        Users user = new Users();
        when(userRepository.findByUserNumber(userNumber)).thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(eq(userNumber), anyList())).thenReturn("newAccessToken");
        when(jwtProvider.createRefreshToken(eq(userNumber))).thenReturn(newRefreshToken);
        when(jwtProvider.getRemainingValidity(newAccessToken)).thenReturn(ttl);

        // 캐시된 Access Token이 없는 상태를 시뮬레이션
        when(valueOperations.get(eq(accessCacheKey))).thenReturn(null);

        Map<String, String> result = tokenService.refreshWithLock(refreshToken);

        // 새로운 Access Token이 Redis에 캐시되었는지 검증
        verify(valueOperations).set(eq(accessCacheKey), eq(newAccessToken), eq(Duration.ofSeconds(ttl)));
        assertThat(result.get("accessToken")).isEqualTo(newAccessToken);
        assertThat(result.get("refreshToken")).isEqualTo(newRefreshToken);
        verify(redisTemplate).delete(lockKey);
    }

    @Test
    @DisplayName("유효하지 않은 RefreshToken 처리 - JwtException")
    void refreshWithLock_invalidToken(){
        String refreshToken = "InvalidRefreshToken";
        String lockKey = "refreshTokenLock:"+refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey),eq("LOCKED"), eq(Duration.ofSeconds(5)))).thenReturn(true);
        when(jwtProvider.validateToken(refreshToken)).thenReturn(false);

        JwtException exception = assertThrows(JwtException.class, () -> tokenService.refreshWithLock(refreshToken));
        assertEquals("유효하지 않은 RefreshToken입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("토큰에 저장된 유저 정보가 존재하지 않는 사용자일때")
    void refreshWithLock_userNotFound() {
        String refreshToken = "validRefreshToken";
        Integer userNumber = 12345;
        String lockKey = "refreshTokenLock:"+refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey),eq("LOCKED"),eq(Duration.ofSeconds(5)))).thenReturn(true);
        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getUserNumber(refreshToken)).thenReturn(userNumber);
        when(userRepository.findByUserNumber(userNumber)).thenReturn(Optional.empty());

        JwtException exception = assertThrows(JwtException.class, () -> tokenService.refreshWithLock(refreshToken));
        assertEquals("저장된 Refresh Token과 일치하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("동시 요청 시 Lock 획득 실패")
    void refreshWithLock_concurrentRequest(){
        String refreshToken = "validRefreshToken";
        String lockKey = "refreshTokenLock:"+refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey),eq("LOCKED"),eq(Duration.ofSeconds(5)))).thenReturn(false);

        JwtException exception = assertThrows(JwtException.class, () -> tokenService.refreshWithLock(refreshToken));
        assertEquals("유효하지 않은 RefreshToken입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("예상치 못한 오류 - 일반 예외")
    void refreshWithLock_unexpectedException(){
        String refreshToken = "validRefreshToken";
        String lockKey = "refreshTokenLock:"+refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey), eq("LOCKED"),eq(Duration.ofSeconds(5)))).thenReturn(true);
        when(jwtProvider.validateToken(refreshToken)).thenThrow(new RuntimeException("Unexpected Error"));

        RuntimeException exception = assertThrows(RuntimeException.class,()->tokenService.refreshWithLock(refreshToken));
        assertEquals("Unexpected Error", exception.getMessage());
    }
    @Test
    @DisplayName("유효한 RefreshToken으로 Redis 캐싱과 TTL 설정 확인")
    void testRefreshTokenCachingAndTTL() {
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        Integer userNumber = 12345;
        long ttl = 900; // 15분
        String lockKey = "refreshTokenLock:" + refreshToken;
        String accessCacheKey = "refreshTokenCache:"+refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey), eq("LOCKED"), eq(Duration.ofSeconds(5))))
                .thenReturn(true);

        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getUserNumber(refreshToken)).thenReturn(userNumber);
        when(valueOperations.get("refreshToken:"+userNumber)).thenReturn(refreshToken);
        Users user = new Users();
        when(userRepository.findByUserNumber(userNumber)).thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(userNumber, List.of("ROLE_USER"))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(userNumber)).thenReturn(newRefreshToken);
        when(jwtProvider.getRemainingValidity(newAccessToken)).thenReturn(ttl);

        when(valueOperations.get(eq(accessCacheKey))).thenReturn(null);
        Map<String, String> result = tokenService.refreshWithLock(refreshToken);

        verify(valueOperations).set(
                eq("refreshTokenCache:" + refreshToken),
                eq(newAccessToken),
                eq(Duration.ofSeconds(ttl))
        );
        assertThat(result.get("accessToken")).isEqualTo(newAccessToken);
        assertThat(result.get("refreshToken")).isEqualTo(newRefreshToken);
        verify(redisTemplate).delete(lockKey);
    }

    @Test
    @DisplayName("Redis TTL 만료 후 캐시 삭제 여부 확인")
    void testRedisTTLExpiration() throws InterruptedException {
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        Integer userNumber = 12345;
        long ttl = 2; // 2초 (테스트를 위해 짧게 설정)
        String lockKey = "refreshTokenLock:" + refreshToken;
        String accessCacheKey = "refreshTokenCache:" + refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey),eq("LOCKED"),eq(Duration.ofSeconds(5))))
                .thenReturn(true);
        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getUserNumber(refreshToken)).thenReturn(userNumber);
        when(valueOperations.get("refreshToken:"+userNumber)).thenReturn(refreshToken);
        Users user = new Users();
        when(userRepository.findByUserNumber(userNumber)).thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(userNumber, List.of("ROLE_USER"))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(userNumber)).thenReturn(newRefreshToken);
        when(jwtProvider.getRemainingValidity(newAccessToken)).thenReturn(ttl);

        // 처음 캐시에는 값이 존재함
        when(valueOperations.get(eq(accessCacheKey))).thenReturn(newAccessToken);

        Map<String, String> result = tokenService.refreshWithLock(refreshToken);
        assertThat(result.get("accessToken")).isEqualTo(newAccessToken);

        // stubbing 충돌을 방지하기 위해 valueOperations 모의를 재설정하고 다시 stubbing
        reset(valueOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq(accessCacheKey))).thenReturn(null);

        // 이후 캐시 만료 후 null 반환
        assertThat(valueOperations.get("refreshTokenCache:" + refreshToken)).isNull();
    }
}
