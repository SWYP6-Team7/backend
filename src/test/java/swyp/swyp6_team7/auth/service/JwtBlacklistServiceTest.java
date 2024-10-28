package swyp.swyp6_team7.auth.service;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import swyp.swyp6_team7.auth.jwt.JwtProvider;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class JwtBlacklistServiceTest {

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
    public void testBlacklistAccessToken() {
        // JWT 생성
        String email = "test@example.com";
        String accessToken = jwtProvider.createAccessToken(email, 1, List.of("ROLE_USER"));

        // 블랙리스트에 등록된 토큰 설정
        Mockito.when(jwtBlacklistService.isTokenBlacklisted(accessToken)).thenReturn(true);

        // 블랙리스트에 등록된 토큰 확인
        boolean isBlacklisted = jwtBlacklistService.isTokenBlacklisted(accessToken);
        assertTrue(isBlacklisted);  // 토큰이 블랙리스트에 등록되었는지 확인
    }

    @Test
    public void testValidateToken_NotBlacklisted() {
        // 새로운 JWT 생성
        String email = "test2@example.com";
        String accessToken = jwtProvider.createAccessToken(email, 2, List.of("ROLE_USER"));

        // 블랙리스트에 등록되지 않았다고 가정
        Mockito.when(jwtBlacklistService.isTokenBlacklisted(accessToken)).thenReturn(false);

        // 블랙리스트에 등록되지 않았으므로 유효해야 함
        boolean isValid = jwtProvider.validateToken(accessToken);
        assertTrue(isValid);  // 토큰이 유효한지 확인
    }

    @Test
    public void testTokenValidation_AfterBlacklist() {
        // JWT 생성
        String email = "test3@example.com";
        String accessToken = jwtProvider.createAccessToken(email, 3, List.of("ROLE_USER"));

        // 블랙리스트에 등록된 토큰 설정
        Mockito.when(jwtBlacklistService.isTokenBlacklisted(accessToken)).thenReturn(true);

        // 블랙리스트에 등록된 토큰은 유효하지 않아야 함
        boolean isValid = jwtProvider.validateToken(accessToken);
        assertFalse(isValid);  // 블랙리스트에 등록된 토큰은 유효하지 않아야 함
    }
}
