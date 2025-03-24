package swyp.swyp6_team7.enrollment.controller;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.enrollment.dto.EnrollmentCreateRequest;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.travel.domain.Travel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class EnrollmentControllerTest extends IntegrationTest {

    private static String jwtToken;
    private static int travelId;
    private static String ownerJwtToken;

    @BeforeAll
    public void setUp() {
        Users user = createUser("enrollment", "password");
        Users newUser = createUser("enrollment2", "password2");
        LoginTokenResponse tokenResponse = login("enrollment2@test.com", "password2");
        jwtToken = tokenResponse.getAccessToken();

        LoginTokenResponse tokenResponse2 = login("enrollment@test.com", "password");
        ownerJwtToken = tokenResponse2.getAccessToken();

        Travel travel = createTravel(user.getUserNumber(), "파리");
        travelId = travel.getNumber();
    }

    @AfterAll
    public void tearDown() {
        deleteTravel(travelId);
    }

    @DisplayName("create: 사용자는 여행 참가 신청을 할 수 있다.")
    @Order(3)
    @Test
    public void create() throws Exception {
        // given
        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .travelNumber(travelId)
                .message("여행 참가 희망")
                .build();
        // when
        ResultActions resultActions = mockMvc.perform(post("/api/enrollment")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("여행 참가 신청이 완료되었습니다."));
    }

    @DisplayName("create: 참가 대상 여행 번호가 없을 경우 예외가 발생한다.")
    @Order(1)
    @Test
    public void createWithoutTravelNumber() throws Exception {
        // given
        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .message("여행 참가 희망")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/enrollment")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.reason").value("여행 참가 신청 시 travelNumber는 필수값입니다."));
    }

    @DisplayName("create: 참가 신청 메시지 길이가 1000자를 넘을 경우 예외가 발생한다.")
    @Order(2)
    @Test
    public void createWithLongMessage() throws Exception {
        // given
        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .travelNumber(travelId)
                .message("*".repeat(1001))
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/enrollment")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.reason").value("여행 참가 신청 메시지는 1000자를 넘을 수 없습니다."));
    }

    @DisplayName("delete: 신청자는 참가 신청을 삭제할 수 있다")
    @Order(6)
    @Test
    public void deleteWhenOwner() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/enrollment/{enrollmentNumber}", 1)
                        .header("Authorization", "Bearer " + jwtToken)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("여행 참가 신청이 취소되었습니다."));
    }

    @DisplayName("accept: 여행 참가 신청을 수락할 수 있다.")
    @Transactional
    @Order(4)
    @Test
    void accept() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(put("/api/enrollment/{enrollmentNumber}/acceptance", 1L)
                .header("Authorization", "Bearer " + ownerJwtToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("여행 참가 신청을 수락했습니다."));
    }

    @DisplayName("reject: 여행 참가 신청을 거절할 수 있다.")
    @Transactional
    @Order(5)
    @Test
    void reject() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/enrollment/{enrollmentNumber}/rejection", 1L)
                .header("Authorization", "Bearer " + ownerJwtToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("여행 참가 신청을 거절했습니다."));
    }

}