package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.entity.Users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TokenControllerTest extends IntegrationTest {

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    @Autowired
    private JwtProvider jwtProvider;

    private Cookie createRefreshTokenCookie(String token) {
        return new Cookie("refreshToken", token);
    }

    @Test
    @DisplayName("새로운 AccessToken 발급 성공")
    @Transactional
    public void testRefreshAccessTokenSuccess() throws Exception {
        Users user = createUser("test", "password");

        LoginTokenResponse response = login("test@test.com", "password");
        String validRefreshToken = response.getRefreshToken();

        Cookie refreshTokenCookie = createRefreshTokenCookie(validRefreshToken);

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.userId").value(user.getUserNumber()))
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
    @Transactional
    public void testRefreshAccessTokenFailureDueToBlacklistedToken() throws Exception {
        // Given
        Users user = createUser("test", "password");

        LoginTokenResponse response = login("test@test.com", "password");
        String refreshToken = response.getRefreshToken();
        Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);

        jwtBlacklistService.addToBlacklist(refreshToken, 1000000L);

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
    @Transactional
    public void testRefreshAccessTokenFailureDueToExpiredToken() throws Exception {
        // Given
        Users user = createUser("test", "password");

        LoginTokenResponse response = login("test@test.com", "password");
        String refreshToken = response.getRefreshToken();
        String newRefreshToken = jwtProvider.createToken(user.getUserNumber(), null, 1);
        Cookie refreshTokenCookie = createRefreshTokenCookie(newRefreshToken);

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.reason").value("Refresh Token이 유효하지 않습니다."))
                .andDo(result -> System.out.println("Expired Refresh Token handled correctly."));
    }

    @Test
    @DisplayName("유효하지 않은 RefreshToken으로 AccessToken 재발급 실패")
    public void testRefreshAccessTokenFailureDueToJwtException() throws Exception {
        // Given
        String invalidRefreshToken = "invalid-refresh-token";
        Cookie refreshTokenCookie = createRefreshTokenCookie(invalidRefreshToken);

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.reason").value("Refresh Token이 유효하지 않습니다."))
                .andDo(result -> System.out.println("Invalid Refresh Token handled correctly."));
    }
}
