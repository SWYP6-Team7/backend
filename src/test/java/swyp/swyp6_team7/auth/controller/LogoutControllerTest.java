package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.auth.service.TokenService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class LogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private UserLoginHistoryService userLoginHistoryService;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private JwtBlacklistService jwtBlacklistService;
    @MockBean
    private TokenService tokenService;
    private Users testUser;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        Users testUser = new Users();
        testUser.setUserEmail("test@example.com");
        testUser.setUserNumber(1);

        CustomUserDetails customUserDetails = new CustomUserDetails(testUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("로그아웃 성공 테스트 - 인증 정보 존재")
    public void testLogoutSuccess() throws Exception {
        String accessToken = "dummyAccessToken";
        long expirationTime = 3600L;

        // Authorization 헤더에 있는 토큰에 대해 검증
        when(jwtProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtProvider.getExpiration(accessToken)).thenReturn(expirationTime);
        // 회원 조회 시 testUser 반환
        when(memberService.findByUserNumber(1)).thenReturn(testUser);

        // 로그아웃 관련 void 메서드들은 아무 작업 없이 처리되도록 모의
        doNothing().when(tokenService).deleteRefreshToken(1);
        doNothing().when(userLoginHistoryService).updateLogoutHistory(testUser);
        doNothing().when(memberService).updateLogoutDate(testUser);

        // 로그아웃 요청 실행. Authorization 헤더와 함께 refreshToken 쿠키 삭제가 수행됨.
        ResultActions resultActions = mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("로그아웃 성공")))
                // refreshToken 쿠키가 maxAge=0으로 설정되어 삭제됨을 검증
                .andExpect(cookie().maxAge("refreshToken", 0));

        // 로그아웃 시 deleteRefreshToken과 블랙리스트 등록이 호출되었는지 검증
        verify(tokenService, times(1)).deleteRefreshToken(1);
        verify(jwtBlacklistService, times(1)).addToBlacklist(accessToken, expirationTime);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - 인증 정보 없음")
    void testLogoutFailureNoAuth() throws Exception {
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access Denied"));
    }
}
