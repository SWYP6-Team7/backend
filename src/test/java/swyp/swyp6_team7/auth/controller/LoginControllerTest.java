package swyp.swyp6_team7.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.auth.service.LoginService;
import swyp.swyp6_team7.auth.service.TokenService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginService loginService;

    @MockBean
    private UserLoginHistoryService userLoginHistoryService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JwtBlacklistService jwtBlacklistService;

    @Test
    @DisplayName("로그인 성공")
    public void testLoginSuccess() throws Exception {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", "mocked-access-token");
        tokenMap.put("refreshToken", "mocked-refresh-token");

        when(loginService.login(any(LoginRequestDto.class))).thenReturn(tokenMap);
        when(loginService.getUserNumberByEmail(eq("test@example.com"))).thenReturn(1);
        when(tokenService.getRefreshTokenValidity()).thenReturn(Duration.ofDays(7).getSeconds());
        Users user = new Users();
        user.setUserNumber(1);
        when(loginService.getUserByEmail(eq("test@example.com"))).thenReturn(user);

        doNothing().when(userLoginHistoryService).saveLoginHistory(any(Users.class));
        doNothing().when(memberService).updateLoginDate(any(Users.class));
        doNothing().when(tokenService).storeRefreshToken(eq(1), eq("mocked-refresh-token"));

        // When & Then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.userId").value("1"))
                .andExpect(jsonPath("$.success.accessToken").value("mocked-access-token"))
                .andExpect(header().string("Set-Cookie", Matchers.containsString("refreshToken=mocked-refresh-token")))
                .andDo(result -> System.out.println("로그인 성공 테스트 완료"));

        verify(loginService, times(1)).login(any(LoginRequestDto.class));
        verify(tokenService, times(1)).storeRefreshToken(1, "mocked-refresh-token");
        verify(userLoginHistoryService, times(1)).saveLoginHistory(user);
        verify(memberService, times(1)).updateLoginDate(user);
    }
}
