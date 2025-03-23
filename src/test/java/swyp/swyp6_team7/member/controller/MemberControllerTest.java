package swyp.swyp6_team7.member.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.service.MemberService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MemberControllerTest extends IntegrationTest {

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("사용가능한 이메일로 가입")
    @Order(1)
    public void testCheckEmailDuplicate() throws Exception {

        doNothing().when(memberService).validateEmail("test@example.com");

        mockMvc.perform(get("/api/users-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk());

        verify(memberService).validateEmail("test@example.com");
    }

    @Test
    @DisplayName("이메일 중복일 떄 예외 발생")
    @Order(3)
    public void testCheckEmailDuplicateExists() throws Exception {

        doThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."))
                .when(memberService).validateEmail("existing@example.com");

        mockMvc.perform(get("/api/users-email")
                        .param("email", "existing@example.com"))
                .andExpect(status().isOk()); // MoingApplicationException

    }
}
