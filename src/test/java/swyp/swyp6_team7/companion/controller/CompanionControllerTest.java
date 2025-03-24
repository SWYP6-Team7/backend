package swyp.swyp6_team7.companion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.companion.service.CompanionService;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.mock.WithMockCustomUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanionService companionService;

    @DisplayName("getTravelCompanions: 특정 여행에 참여 확정된 사용자 정보를 가져올 수 있다.")
    @WithMockCustomUser
    @Test
    void getTravelCompanions() throws Exception {
        // given
        CompanionInfoDto companion1 = new CompanionInfoDto(1, "유저1", AgeGroup.TEEN, "profile-1");
        given(companionService.findCompanionsByTravelNumber(anyInt()))
                .willReturn(List.of(companion1));

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travel/{travelNumber}/companions", 1));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.totalCount").value(1))
                .andExpect(jsonPath("$.success.companions[0].userNumber").value(1))
                .andExpect(jsonPath("$.success.companions[0].userName").value("유저1"))
                .andExpect(jsonPath("$.success.companions[0].ageGroup").value("10대"))
                .andExpect(jsonPath("$.success.companions[0].profileUrl").value("profile-1"));
    }
}