package swyp.swyp6_team7.travel.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.dto.EnrollmentResponse;
import swyp.swyp6_team7.enrollment.service.EnrollmentService;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.mock.WithMockCustomUser;
import swyp.swyp6_team7.travel.dto.request.TravelEnrollmentLastViewedRequest;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentsResponse;
import swyp.swyp6_team7.travel.service.TravelService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TravelEnrollmentControllerTest extends IntegrationTest {

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private TravelService travelService;


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
                .andExpect(jsonPath("$.success.totalCount").value(1))
                .andExpect(jsonPath("$.success.enrollments[0].enrollmentNumber").value(1))
                .andExpect(jsonPath("$.success.enrollments[0].userName").value("신청자명"))
                .andExpect(jsonPath("$.success.enrollments[0].userAgeGroup").value("10대"))
                .andExpect(jsonPath("$.success.enrollments[0].profileUrl").value("profile-url"))
                .andExpect(jsonPath("$.success.enrollments[0].message").value("여행 신청"))
                .andExpect(jsonPath("$.success.enrollments[0].status").value("대기"));
    }

    @DisplayName("getEnrollmentsLastViewedTime: 여행 신청 목록 lastViewedAt을 조회한다.")
    @WithMockCustomUser
    @Test
    void getEnrollmentsLastViewedTime() throws Exception {
        // given

        LocalDateTime enrollmentLastViewedAt = LocalDateTime.of(2024, 11, 17, 12, 0);
        given(travelService.getEnrollmentsLastViewedAt(anyInt()))
                .willReturn(enrollmentLastViewedAt);


        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travel/{travelNumber}/enrollments/last-viewed", 1));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.lastViewedAt").value("2024.11.17 12:00"));
    }

    @DisplayName("updateEnrollmentsLastViewedTime: 여행 신청 목록 lastViewedAt을 수정한다.")
    @WithMockCustomUser
    @Test
    void updateEnrollmentsLastViewedTime() throws Exception {
        // given
        LocalDateTime lastViewedAt = LocalDateTime.of(2024, 11, 17, 12, 0);
        TravelEnrollmentLastViewedRequest request = TravelEnrollmentLastViewedRequest.builder()
                .lastViewedAt(lastViewedAt)
                .build();

        doNothing().when(travelService).updateEnrollmentLastViewedAt(anyInt(), any(LocalDateTime.class));

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/travel/{travelNumber}/enrollments/last-viewed", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("신청 목록 LastViewedAt 수정 완료"));
    }

    @DisplayName("getEnrollmentsCount: 특정 여행에 대해 PENDING 상태의 신청 수를 가져올 수 있다.")
    @WithMockCustomUser
    @Test
    void getEnrollmentsCount() throws Exception {
        // given
        given(enrollmentService.getPendingEnrollmentsCountByTravelNumber(anyInt()))
                .willReturn(2L);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travel/{travelNumber}/enrollmentCount", 1));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("2"));
    }
}