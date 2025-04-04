package swyp.swyp6_team7.member.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.global.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class MemberControllerTest extends IntegrationTest {

    @Test
    @DisplayName("사용가능한 이메일로 가입")
    public void testCheckEmailDuplicate() throws Exception {

        mockMvc.perform(get("/api/users-email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 중복일 떄 예외 발생")
    public void testCheckEmailDuplicateExists() throws Exception {
        createUser("test", "password");

        mockMvc.perform(get("/api/users-email")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk()); // MoingApplicationException

    }
}
