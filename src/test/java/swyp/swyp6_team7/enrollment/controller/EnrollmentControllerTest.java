package swyp.swyp6_team7.enrollment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.enrollment.dto.EnrollmentCreateRequest;
import swyp.swyp6_team7.enrollment.service.EnrollmentService;
import swyp.swyp6_team7.mock.WithMockCustomUser;
import swyp.swyp6_team7.notification.service.NotificationService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private NotificationService notificationService;


    @DisplayName("create: 사용자는 여행 참가 신청을 할 수 있다.")
    @WithMockCustomUser
    @Test
    public void create() throws Exception {
        // given
        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .travelNumber(1)
                .message("여행 참가 희망")
                .build();

        doNothing().when(enrollmentService)
                .create(any(EnrollmentCreateRequest.class), any(Integer.class), any(LocalDate.class));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/enrollment")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string("여행 참가 신청이 완료되었습니다."));
    }

    @DisplayName("create: 참가 대상 여행 번호가 없을 경우 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    public void createWithoutTravelNumber() throws Exception {
        // given
        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .message("여행 참가 희망")
                .build();

        doNothing().when(enrollmentService)
                .create(any(EnrollmentCreateRequest.class), any(Integer.class), any(LocalDate.class));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/enrollment")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("여행 참가 신청 시 travelNumber는 필수값입니다."));
    }

    @DisplayName("create: 참가 신청 메시지 길이가 1000자를 넘을 경우 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    public void createWithLongMessage() throws Exception {
        // given
        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .travelNumber(1)
                .message("*" .repeat(1001))
                .build();

        doNothing().when(enrollmentService)
                .create(any(EnrollmentCreateRequest.class), any(Integer.class), any(LocalDate.class));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/enrollment")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("여행 참가 신청 메시지는 1000자를 넘을 수 없습니다."));
    }

    @DisplayName("delete: 신청자는 참가 신청을 삭제할 수 있다")
    @WithMockCustomUser
    @Test
    public void deleteWhenOwner() throws Exception {
        // given
        doNothing().when(enrollmentService).delete(any(Long.class), any(Integer.class));

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/enrollment/{enrollmentNumber}", 1));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string("여행 참가 신청이 취소되었습니다."));
    }

    @DisplayName("accept: 여행 참가 신청을 수락할 수 있다.")
    @WithMockCustomUser
    @Test
    void accept() throws Exception {
        // given
        doNothing().when(enrollmentService)
                .accept(any(Long.class), any(Integer.class));

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/enrollment/{enrollmentNumber}/acceptance", 1L)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("여행 참가 신청을 수락했습니다."));
    }

}