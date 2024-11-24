package swyp.swyp6_team7.member.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private UserLoginHistoryService userLoginHistoryService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserDetailsService userDetailsService;

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

        // Mock: memberService.signUp() 호출 시 반환 값 설정
        Mockito.when(memberService.signUp(any(UserRequestDto.class))).thenReturn(response);

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
        // given
        UserRequestDto userRequestDto = new UserRequestDto(
                "test@example.com",
                "password",
                "testuser",
                "M",
                "20대",
                Set.of("국내", "가성비")
        );

        // Mock: 이메일 중복일 때 예외 발생
        Mockito.when(memberService.signUp(any(UserRequestDto.class)))
                .thenThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."));

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
        // 이메일 중복 확인이 통과하는 경우
        Mockito.doNothing().when(memberService).validateEmail("test@example.com");

        // when & then
        mockMvc.perform(get("/api/users-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("사용 가능한 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 중복일 떄 예외 발생")
    public void testCheckEmailDuplicateExists() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."))
                .when(memberService).validateEmail("test@example.com");

        // when & then
        mockMvc.perform(get("/api/users-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("관리자계정 생성테스트")
    public void testCreateAdmin() throws Exception {
        UserRequestDto adminRequest = new UserRequestDto(
                "admin@example.com",
                "adminpassword",
                "adminuser",
                "M",
                "30대",
                Set.of("관리자")
        );

        // 반환할 가짜 값 (예: 생성된 관리자 정보 또는 결과 메시지)
        Map<String, Object> response = new HashMap<>();
        response.put("userNumber", 1);
        response.put("email", "admin@example.com");
        response.put("message", "Admin successfully registered");

        // Mock: 관리자 생성 메서드 호출을 목 처리
        Mockito.when(memberService.createAdmin(any(UserRequestDto.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/admins/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"admin@example.com\", \"password\": \"adminpassword\", \"name\": \"adminuser\", \"gender\": \"M\", \"agegroup\": \"30대\", \"preferredTags\": [\"관리자\"] }"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Admin successfully registered"));
    }
}
