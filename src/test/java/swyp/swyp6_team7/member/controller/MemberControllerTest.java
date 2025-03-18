package swyp.swyp6_team7.member.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.dto.UserRequestDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MemberControllerTest extends IntegrationTest {

    @Test
    @DisplayName("일반이메일 가입 성공테스트")
    public void testSignUpSuccess() throws Exception {
        // given
        UserRequestDto userRequestDto = new UserRequestDto(
                "test@example.com",
                "password",
                "testuser",
                "M",
                "20대",
                Set.of("국내", "가성비")
        );

        Map<String, Object> response = new HashMap<>();
        response.put("userNumber", 1);
        response.put("email", "test@example.com");
        response.put("accessToken", "jwt-token");

        // when & then
        mockMvc.perform(post("/api/users/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"test@example.com\", \"password\": \"password\", \"name\": \"testuser\", \"gender\": \"M\", \"agegroup\": \"20대\", \"preferredTags\": [\"국내\", \"가성비\"] }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 가입")
    public void testSignUpEmailAlreadyExists() throws Exception {

        // when & then
        mockMvc.perform(post("/api/users/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"test@example.com\", \"password\": \"password\", \"name\": \"testuser\", \"gender\": \"M\", \"agegroup\": \"20대\", \"preferredTags\": [\"국내\", \"가성비\"] }"))
                .andExpect(status().isConflict())  // 409 Conflict
                .andExpect(jsonPath("$.error").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("사용가능한 이메일로 가입")
    public void testCheckEmailDuplicate() throws Exception {

        // when & then
        mockMvc.perform(get("/api/users-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("사용 가능한 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 중복일 떄 예외 발생")
    public void testCheckEmailDuplicateExists() throws Exception {

        // when & then
        mockMvc.perform(get("/api/users-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.reason").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("관리자계정 생성테스트")
    public void testCreateAdmin() throws Exception {

        // 반환할 가짜 값 (예: 생성된 관리자 정보 또는 결과 메시지)
        Map<String, Object> response = new HashMap<>();
        response.put("userNumber", 1);
        response.put("email", "admin@example.com");
        response.put("message", "Admin successfully registered");
        
        // when & then
        mockMvc.perform(post("/api/admins/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"admin@example.com\", \"password\": \"adminpassword\", \"name\": \"adminuser\", \"gender\": \"M\", \"agegroup\": \"30대\", \"preferredTags\": [\"관리자\"] }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("Admin successfully registered"));
    }
}
