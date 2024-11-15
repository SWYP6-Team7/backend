package swyp.swyp6_team7.travel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.dto.EnrollmentResponse;
import swyp.swyp6_team7.enrollment.service.EnrollmentService;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.mock.WithMockCustomUser;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentsResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TravelEnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;


    @DisplayName("findEnrollments: 특정 여행에 대한 참가 신청서 목록을 조회할 수 있다")
    @WithMockCustomUser
    @Test
    public void findEnrollments() throws Exception {
        // given
        EnrollmentResponse enrollment1 = EnrollmentResponse.builder()
                .enrollmentNumber(1)
                .userName("신청자명")
                .ageGroup(AgeGroup.TEEN)
                .profileUrl("profile-url")
                .message("여행 신청")
                .status(EnrollmentStatus.PENDING)
                .build();

        TravelEnrollmentsResponse response = TravelEnrollmentsResponse.from(List.of(enrollment1));

        given(enrollmentService.findEnrollmentsByTravelNumber(anyInt(), anyInt()))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travel/{travelNumber}/enrollments", 1));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.enrollments[0].enrollmentNumber").value(1))
                .andExpect(jsonPath("$.enrollments[0].userName").value("신청자명"))
                .andExpect(jsonPath("$.enrollments[0].userAgeGroup").value("10대"))
                .andExpect(jsonPath("$.enrollments[0].profileUrl").value("profile-url"))
                .andExpect(jsonPath("$.enrollments[0].message").value("여행 신청"))
                .andExpect(jsonPath("$.enrollments[0].status").value("대기"));
    }

}