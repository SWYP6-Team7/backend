package swyp.swyp6_team7.auth.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.security.core.GrantedAuthority;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "kakao.client-id=fake-client-id",
        "kakao.client-secret=fake-client-secret",
        "kakao.redirect-uri=http://localhost:8080/login/oauth2/code/kakao",
        "kakao.token-url=https://kauth.kakao.com/oauth/token",
        "kakao.user-info-url=https://kapi.kakao.com/v2/user/me"
})
public class LogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private UserLoginHistoryService userLoginHistoryService;
    @MockBean
    private JwtProvider jwtProvider;

    @Test
    public void testLogoutSuccess() throws Exception {
        Users user = new Users();
        user.setUserEmail("test@example.com");

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(memberService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 성공"));
    }

    @Test
    public void testLogoutFailureNoUserLoggedIn() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isForbidden());
    }
}
