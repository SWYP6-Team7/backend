package swyp.swyp6_team7.auth.controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "kakao.client-id=fake-client-id",
        "kakao.client-secret=fake-client-secret",
        "kakao.redirect-uri=http://localhost:8080/login/oauth2/code/kakao",
        "kakao.token-url=https://kauth.kakao.com/oauth/token",
        "kakao.user-info-url=https://kapi.kakao.com/v2/user/me"
})
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtBlacklistService jwtBlacklistService;

    @Test
    public void testRefreshAccessTokenSuccess() throws Exception {
        // Given
        String validRefreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String userEmail = "test@example.com";

        Cookie refreshTokenCookie = new Cookie("refreshToken", validRefreshToken);

        Users user = new Users();
        user.setUserEmail(userEmail);
        user.setUserNumber(123);

        when(jwtProvider.validateToken(validRefreshToken)).thenReturn(true);
        when(jwtProvider.getUserEmail(validRefreshToken)).thenReturn(userEmail);
        when(userRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(user.getUserEmail(), user.getUserNumber(), List.of(user.getRole().name())))
                .thenReturn(newAccessToken);
        when(jwtBlacklistService.isTokenBlacklisted(validRefreshToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken));
    }

    @Test
    public void testRefreshAccessTokenFailureDueToMissingRefreshToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Refresh Token이 존재하지 않습니다."));
    }

    @Test
    public void testRefreshAccessTokenFailureDueToInvalidToken() throws Exception {
        // Given
        String invalidRefreshToken = "invalid-refresh-token";

        Cookie refreshTokenCookie = new Cookie("refreshToken", invalidRefreshToken);

        when(jwtProvider.validateToken(invalidRefreshToken)).thenReturn(false);
        when(jwtBlacklistService.isTokenBlacklisted(invalidRefreshToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh Token이 만료되었습니다. 다시 로그인 해주세요."));
    }

    @Test
    public void testRefreshAccessTokenFailureDueToJwtException() throws Exception {
        // Given
        String refreshToken = "refresh-token";
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

        when(jwtProvider.validateToken(refreshToken)).thenThrow(new JwtException("Invalid JWT token"));
        when(jwtBlacklistService.isTokenBlacklisted(refreshToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Refresh Token이 유효하지 않습니다."));
    }
    @Test
    public void testRefreshAccessTokenFailureDueToBlacklistedToken() throws Exception {
        // Given
        String blacklistedRefreshToken = "blacklisted-refresh-token";

        Cookie refreshTokenCookie = new Cookie("refreshToken", blacklistedRefreshToken);

        when(jwtBlacklistService.isTokenBlacklisted(blacklistedRefreshToken)).thenReturn(true); // 블랙리스트에 존재함

        // When & Then
        mockMvc.perform(post("/api/token/refresh")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh Token이 블랙리스트에 있습니다. 다시 로그인 해주세요."));
    }
}
