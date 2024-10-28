package swyp.swyp6_team7.auth.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

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
        String token = jwtProvider.createAccessToken(userEmail, userNumber, roles);

        // Then
        assertNotNull(token);  // 토큰이 null이 아니어야 함
        assertTrue(jwtProvider.validateToken(token));  // 토큰이 유효해야 함
    }

    @Test
    void testCreateRefreshToken() {
        // Given
        String userEmail = "test@example.com";
        Integer userNumber = 1;

        // When
        String refreshToken = jwtProvider.createRefreshToken(userEmail, userNumber);

        // Then
        assertNotNull(refreshToken);  // Refresh 토큰이 null이 아니어야 함
        assertTrue(jwtProvider.validateToken(refreshToken));  // Refresh 토큰이 유효해야 함
    }

    @Test
    void testGetUserEmailFromToken() {
        // Given
        String userEmail = "test@example.com";
        Integer userNumber = 1;
        List<String> roles = List.of("ROLE_USER");
        String token = jwtProvider.createAccessToken(userEmail, userNumber, roles);

        // When
        String extractedEmail = jwtProvider.getUserEmail(token);

        // Then
        assertEquals(userEmail, extractedEmail);  // 추출된 이메일이 원래 이메일과 같아야 함
    }

    @Test
    void testGetUserNumberFromToken() {
        // Given
        String userEmail = "test@example.com";
        Integer userNumber = 1;
        List<String> roles = List.of("ROLE_USER");
        String token = jwtProvider.createAccessToken(userEmail, userNumber, roles);

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
        String refreshToken = jwtProvider.createRefreshToken(userEmail, userNumber);

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
}