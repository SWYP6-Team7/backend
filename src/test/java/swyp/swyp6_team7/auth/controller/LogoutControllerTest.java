package swyp.swyp6_team7.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.global.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LogoutControllerTest extends IntegrationTest {

    private String userName = "test";
    private String email = "test@test.com";
    private String password = "password";

    @Test
    @DisplayName("로그아웃 성공 테스트 - 인증 정보 존재")
    @Transactional
    public void testLogoutSuccess() throws Exception {
        // 테스트용 사용자 생성
        createUser(userName, password);
        // 로그인 시도
        LoginTokenResponse response = login(email, password);

        String accessToken = response.getAccessToken();

        // 로그아웃 요청 실행. Authorization 헤더와 함께 refreshToken 쿠키 삭제가 수행됨.
        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("로그아웃 성공"))
                // refreshToken 쿠키가 maxAge=0으로 설정되어 삭제됨을 검증
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - 인증 정보 없음")
    void testLogoutFailureNoAuth() throws Exception {
        mockMvc.perform(post("/api/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
