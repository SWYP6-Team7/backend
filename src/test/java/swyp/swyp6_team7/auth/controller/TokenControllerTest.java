package swyp.swyp6_team7.auth.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.auth.service.TokenService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private JwtBlacklistService jwtBlacklistService;

    private Cookie createRefreshTokenCookie(String token) {
        return new Cookie("refreshToken", token);
    }

    @BeforeEach
    public void setUp() {
        Mockito.clearInvocations(jwtProvider, jwtBlacklistService, tokenService);
    }

    @Test
    @DisplayName("새로운 AccessToken 발급 성공")
    public void testRefreshAccessTokenSuccess() throws Exception {
        String validRefreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        Integer userNumber = 123;

        Cookie refreshTokenCookie = createRefreshTokenCookie(validRefreshToken);

        when(jwtBlacklistService.isTokenBlacklisted(validRefreshToken)).thenReturn(false);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", newAccessToken);
        tokenMap.put("refreshToken", newRefreshToken);
        when(tokenService.refreshWithLock(validRefreshToken)).thenReturn(tokenMap);

        when(jwtProvider.getUserNumber(newAccessToken)).thenReturn(userNumber);

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.userId").value(userNumber))
                .andExpect(jsonPath("$.success.accessToken").value(newAccessToken))
                .andDo(result -> System.out.println("Access Token successfully refreshed."));
    }

    @Test
    @DisplayName("AccessToken 재발급 실패 - RefreshToken 미존재")
    public void testRefreshAccessTokenFailureDueToMissingRefreshToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.reason").value("Refresh Token이 존재하지 않습니다."))
                .andDo(result -> System.out.println("Missing Refresh Token handled correctly."));
    }

    @Test
    @DisplayName("블랙리스트에 등록된 RefreshToken으로 AccessToken 재발급 실패")
    public void testRefreshAccessTokenFailureDueToBlacklistedToken() throws Exception {
        // Given
        String blacklistedRefreshToken = "blacklisted-refresh-token";
        Cookie refreshTokenCookie = createRefreshTokenCookie(blacklistedRefreshToken);

        // Mock 설정
        when(jwtBlacklistService.isTokenBlacklisted(blacklistedRefreshToken)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.reason").value("Refresh Token이 블랙리스트에 있습니다. 다시 로그인 해주세요."))
                .andDo(result -> System.out.println("Blacklisted Refresh Token handled correctly."));
    }

    @Test
    @DisplayName("만료된 RefreshToken으로 AccessToken 재발급 실패")
    public void testRefreshAccessTokenFailureDueToExpiredToken() throws Exception {
        // Given
        String expiredRefreshToken = "expired-refresh-token";
        Cookie refreshTokenCookie = createRefreshTokenCookie(expiredRefreshToken);

        // Mock 설정
        when(jwtBlacklistService.isTokenBlacklisted(expiredRefreshToken)).thenReturn(false);
        when(tokenService.refreshWithLock(expiredRefreshToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.reason").value("Refresh Token이 만료되었습니다. 다시 로그인 해주세요."))
                .andDo(result -> System.out.println("Expired Refresh Token handled correctly."));
    }

    @Test
    @DisplayName("유효하지 않은 RefreshToken으로 AccessToken 재발급 실패")
    public void testRefreshAccessTokenFailureDueToJwtException() throws Exception {
        // Given
        String invalidRefreshToken = "invalid-refresh-token";
        Cookie refreshTokenCookie = createRefreshTokenCookie(invalidRefreshToken);

        // Mock 설정
        when(jwtBlacklistService.isTokenBlacklisted(invalidRefreshToken)).thenReturn(false);
        when(tokenService.refreshWithLock(invalidRefreshToken)).thenThrow(new JwtException("Invalid JWT token"));

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.reason").value("Refresh Token이 유효하지 않습니다."))
                .andDo(result -> System.out.println("Invalid Refresh Token handled correctly."));
    }

    @Test
    @DisplayName("알 수 없는 서버 에러 발생 시 AccessToken 재발급 실패")
    public void testRefreshAccessTokenFailureDueToUnexpectedError() throws Exception {
        // Given
        String refreshToken = "refresh-token";
        Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);

        // Mock 설정
        when(jwtBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);
        when(tokenService.refreshWithLock(refreshToken)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.error.reason").value("Access Token 재발급에 실패했습니다."))
                .andDo(result -> System.out.println("Unexpected error handled correctly."));
    }
}
