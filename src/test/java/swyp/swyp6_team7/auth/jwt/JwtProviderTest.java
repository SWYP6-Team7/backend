package swyp.swyp6_team7.auth.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private JwtBlacklistService jwtBlacklistService;

    @BeforeEach
    public void setUp() {
        // JwtBlacklistService를 Mockito로 모킹
        jwtBlacklistService = Mockito.mock(JwtBlacklistService.class);

        // 안전한 256비트의 SecretKey 생성
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // SecretKey를 Base64로 인코딩하여 String으로 변환
        String encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        // JwtProvider에 Base64 인코딩된 secretKey와 모킹된 JwtBlacklistService 전달
        jwtProvider = new JwtProvider(encodedSecretKey, jwtBlacklistService);  // String 전달
    }

    @Test
    void testCreateAccessToken() {
        // Given
        String userEmail = "test@example.com";
        Integer userNumber = 1;
        List<String> roles = List.of("ROLE_USER");

        // When
        String token = jwtProvider.createAccessToken(userNumber, roles);

        // Then
        assertNotNull(token);  // 토큰이 null이 아니어야 함
        assertTrue(jwtProvider.validateToken(token));  // 토큰이 유효해야 함
    }

    @Test
    void testCreateRefreshToken() {
        // Given
        Integer userNumber = 1;

        // When
        String refreshToken = jwtProvider.createRefreshToken(userNumber);

        // Then
        assertNotNull(refreshToken);  // Refresh 토큰이 null이 아니어야 함
        assertTrue(jwtProvider.validateToken(refreshToken));  // Refresh 토큰이 유효해야 함
    }


    @Test
    void testGetUserNumberFromToken() {
        // Given
        String userEmail = "test@example.com";
        Integer userNumber = 1;
        List<String> roles = List.of("ROLE_USER");
        String token = jwtProvider.createAccessToken( userNumber, roles);

        // When
        Integer extractedUserNumber = jwtProvider.getUserNumber(token);

        // Then
        assertEquals(userNumber, extractedUserNumber);  // 추출된 사용자 ID가 원래 ID와 같아야 함
    }

    @Test
    void testValidateInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);  // 잘못된 토큰은 유효하지 않아야 함
    }

    @Test
    void testRefreshAccessTokenWithValidRefreshToken() {
        // Given
        String userEmail = "test@example.com";
        Integer userNumber = 1;
        String refreshToken = jwtProvider.createRefreshToken( userNumber);

        // When
        String newAccessToken = jwtProvider.refreshAccessToken(refreshToken);

        // Then
        assertNotNull(newAccessToken);  // 새로운 Access Token이 생성되어야 함
        assertTrue(jwtProvider.validateToken(newAccessToken));  // 새 Access Token이 유효해야 함
    }

    @Test
    void testRefreshAccessTokenWithInvalidRefreshToken() {
        // Given
        String invalidRefreshToken = "invalid.token.here";

        // When & Then
        assertThrows(JwtException.class, () -> jwtProvider.refreshAccessToken(invalidRefreshToken));  // 잘못된 토큰으로 리프레시 요청 시 예외가 발생해야 함
    }

    @Test
    void testRefreshAccessTokenAfterAccessTokenExpiration() throws InterruptedException {
        // Given
        String userEmail = "test@example.com";
        Integer userNumber = 1;
        List<String> roles = List.of("ROLE_USER");

        // SecretKey 직접 생성
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // 만료된 Access Token 생성 (현재보다 1초 이전으로 만료 시간 설정)
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() - 900000); // 현재 시간보다 1초 전
        String expiredAccessToken = Jwts.builder()
                .setSubject(userEmail)
                .claim("userNumber", userNumber)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = jwtProvider.createRefreshToken(userNumber);

        // 만료된 Access Token을 리프레시 토큰으로 재발급
        assertFalse(jwtProvider.validateToken(expiredAccessToken));  // 만료된 토큰은 유효하지 않아야 함

        // When: 만료된 Access Token을 Refresh Token으로 재발급 요청
        String newAccessToken = jwtProvider.refreshAccessToken(refreshToken);

        // Then
        assertNotNull(newAccessToken);  // 새로운 Access Token이 생성되어야 함
        assertTrue(jwtProvider.validateToken(newAccessToken));  // 새 Access Token이 유효해야 함
        assertNotEquals(expiredAccessToken, newAccessToken);  // 새로 발급된 Access Token은 이전 것과 달라야 함
    }
}