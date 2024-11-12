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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(status().isCreated());
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

    /*
    @DisplayName("delete: 신청자는 참가 신청을 삭제할 수 있다")
    @Test
    public void deleteWhenOwner() throws Exception {
        // given
        String url = "/api/enrollment/{enrollmentNumber}";
        createTestTravel(2, LocalDate.now().plusDays(1), TravelStatus.IN_PROGRESS);
        Enrollment enrollment = enrollmentRepository.save(Enrollment.builder()
                .userNumber(user.getUserNumber())
                .travelNumber(travel.getNumber())
                .message("참가 신청합니다.")
                .status(EnrollmentStatus.PENDING)
                .build()
        );

        // when
        ResultActions resultActions = mockMvc.perform(delete(url, enrollment.getNumber()));

        // then
        resultActions
                .andExpect(status().isNoContent());

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertThat(enrollments).isEmpty();
    }

    @DisplayName("delete: 신청자가 아닐 경우 참가 신청을 삭제할 수 없다")
    @Test
    public void deleteWhenNotOwner() throws Exception {
        // given
        String url = "/api/enrollment/{enrollmentNumber}";
        createTestTravel(2, LocalDate.now().plusDays(1), TravelStatus.IN_PROGRESS);
        Users owner = userRepository.save(Users.builder()
                .userEmail("owner@test.com")
                .userPw("1234")
                .userName("host")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TEEN)
                .userRegDate(LocalDateTime.now())
                .userStatus(UserStatus.ABLE)
                .build()
        );

        Enrollment enrollment = enrollmentRepository.save(Enrollment.builder()
                .userNumber(owner.getUserNumber())
                .travelNumber(travel.getNumber())
                .message("참가 신청합니다.")
                .status(EnrollmentStatus.PENDING)
                .build()
        );

        // when
        ResultActions resultActions = mockMvc.perform(delete(url, enrollment.getNumber()));

        // then
        resultActions
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("서버 에러: " + "접근 권한이 없는 신청서입니다."));
    }*/

}