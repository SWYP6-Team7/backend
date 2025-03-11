package swyp.swyp6_team7.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.auth.service.LoginFacade;
import swyp.swyp6_team7.auth.service.TokenService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginFacade loginFacade;

    @MockBean
    private UserLoginHistoryService userLoginHistoryService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private TokenService tokenService;

    @Test
    @DisplayName("로그인 성공")
    public void testLoginSuccess() throws Exception {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        LoginTokenResponse tokenResponse = new LoginTokenResponse(1, "mocked-access-token", "mocked-refresh-token");

        when(loginFacade.login(any(LoginRequestDto.class))).thenReturn(tokenResponse);
        when(loginFacade.getUserNumberByEmail(eq("test@example.com"))).thenReturn(1);
        when(tokenService.getRefreshTokenValidity()).thenReturn(Duration.ofDays(7).getSeconds());
        Users user = new Users();
        user.setUserNumber(1);
        when(loginFacade.getUserByEmail(eq("test@example.com"))).thenReturn(user);

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
//                .andExpect(header().string("Set-Cookie", Matchers.containsString("refreshToken=mocked-refresh-token")))
                .andDo(result -> System.out.println("로그인 성공 테스트 완료"));

        verify(loginFacade, times(1)).login(any(LoginRequestDto.class));
        verify(tokenService, times(1)).storeRefreshToken(1, "mocked-refresh-token");
        verify(userLoginHistoryService, times(1)).saveLoginHistory(user);
        verify(memberService, times(1)).updateLoginDate(user);
    }
}
