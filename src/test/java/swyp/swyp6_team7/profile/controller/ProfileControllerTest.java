package swyp.swyp6_team7.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.global.utils.auth.MemberAuthorizeUtil;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.profile.dto.ProfileUpdateRequest;
import swyp.swyp6_team7.profile.service.ProfileService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    private String validToken;
    private Integer userNumber;
    private Users user;

    @BeforeEach
    void setUp() {
        userNumber = 1;
    }

    @Test
    @WithMockUser
    @DisplayName("프로필 수정 테스트 성공")
    void testUpdateProfile_Success() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("Test Name");
        request.setPreferredTags(new String[]{"Tag1", "Tag2"});

        try (MockedStatic<MemberAuthorizeUtil> mockedStatic = mockStatic(MemberAuthorizeUtil.class)) {
            mockedStatic.when(MemberAuthorizeUtil::getLoginUserNumber).thenReturn(userNumber);

            mockMvc.perform(put("/api/profile/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value("프로필 업데이트 완료"));

            verify(profileService).updateProfile(eq(userNumber), any(ProfileUpdateRequest.class));
        }
    }

    @Test
    @WithMockUser
    @DisplayName("프로필 조회 테스트 성공")
    void testViewProfile_Success() throws Exception {

        try (MockedStatic<MemberAuthorizeUtil> mockedStatic = mockStatic(MemberAuthorizeUtil.class)) {
            mockedStatic.when(MemberAuthorizeUtil::getLoginUserNumber).thenReturn(userNumber);

            mockMvc.perform(get("/api/profile/me"))
                    .andExpect(status().isOk());
        }
    }

}
