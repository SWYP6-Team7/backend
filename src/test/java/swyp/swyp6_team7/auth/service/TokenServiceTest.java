package swyp.swyp6_team7.auth.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TokenServiceTest {
    private TokenService tokenService;
    private JwtProvider jwtProvider;
    private RedisTemplate<String, String> redisTemplate;
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

        when(valueOperations.setIfAbsent(eq(lockKey), eq("LOCKED"),eq(Duration.ofSeconds(5)))).thenReturn(true);
        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getUserNumber(refreshToken)).thenReturn(userNumber);
        when(userRepository.findByUserNumber(userNumber)).thenReturn(Optional.of(new Users()));
        when(jwtProvider.createAccessToken(eq(userNumber), anyList())).thenReturn("newAccessToken");

        String result = tokenService.refreshWithLock(refreshToken);

        assertEquals("newAccessToken", result);
        verify(redisTemplate).delete(lockKey);
    }

    @Test
    @DisplayName("유효하지 않은 토큰일 떄 - JwtException")
    void refreshWithLock_invalidToken(){
        String refreshToken = "InvalidRefreshToken";
        String lockKey = "refreshTokenLock:"+refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey),eq("LOCKED"), eq(Duration.ofSeconds(5)))).thenReturn(true);
        when(jwtProvider.validateToken(refreshToken)).thenReturn(false);

        JwtException exception = assertThrows(JwtException.class, () -> tokenService.refreshWithLock(refreshToken));
        assertEquals("유효하지 않은 RefreshToken입니다.", exception.getMessage());
        verify(redisTemplate).delete(lockKey);
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

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> tokenService.refreshWithLock(refreshToken));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(redisTemplate).delete(lockKey);
    }

    @Test
    @DisplayName("동시 요청으로 Lock 획득 실패")
    void refreshWithLock_concurrentRequest(){
        String refreshToken = "validRefreshToken";
        String lockKey = "refreshTokenLock:"+refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey),eq("LOCKED"),eq(Duration.ofSeconds(5)))).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> tokenService.refreshWithLock(refreshToken));
        assertEquals("동시 요청으로 인해 처리 중단: "+refreshToken, exception.getMessage());
        verify(redisTemplate, never()).delete(lockKey);
    }

    @Test
    @DisplayName("예상치 못한 오류 - 일반 예외")
    void refreshWithLock_unexpectedException(){
        String refreshToken = "validRefreshToken";
        String lockKey = "refreshTokenLock:"+refreshToken;

        when(valueOperations.setIfAbsent(eq(lockKey), eq("LOCKED"),eq(Duration.ofSeconds(5)))).thenReturn(true);
        when(jwtProvider.validateToken(refreshToken)).thenThrow(new RuntimeException("Unexpected Error"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,()->tokenService.refreshWithLock(refreshToken));
        assertEquals("예기치 못한 오류가 발생했습니다.", exception.getMessage());
        verify(redisTemplate).delete(lockKey);
    }
}
