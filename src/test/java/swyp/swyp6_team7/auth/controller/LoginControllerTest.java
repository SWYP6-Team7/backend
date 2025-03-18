package swyp.swyp6_team7.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.global.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginControllerTest extends IntegrationTest {

    @Test
    @DisplayName("로그인 성공")
    @Transactional
    public void testLoginSuccess() throws Exception {
        // Given
        createUser("test", "password");

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password");

        // When & Then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.userId").value("1"))
                .andExpect(jsonPath("$.success.accessToken").exists())
                .andExpect(header().exists("Set-Cookie"))
                .andDo(result -> System.out.println("로그인 성공 테스트 완료"));
    }
}
