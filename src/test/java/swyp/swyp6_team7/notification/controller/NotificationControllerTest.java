package swyp.swyp6_team7.notification.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.mock.WithMockCustomUser;
import swyp.swyp6_team7.notification.dto.NotificationDto;
import swyp.swyp6_team7.notification.dto.TravelNotificationDto;
import swyp.swyp6_team7.notification.entity.Notification;
import swyp.swyp6_team7.notification.entity.TravelNotification;
import swyp.swyp6_team7.notification.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @DisplayName("getNotifications: 사용자의 알림 목록을 조회할 수 있다.")
    @WithMockCustomUser
    @Test
    void getNotificationsByUser() throws Exception {
        // given
        Notification notification = Notification.builder()
                .receiverNumber(1)
                .title("알림")
                .content("내용")
                .createdAt(LocalDateTime.of(2024, 11, 20, 0, 0))
                .isRead(false)
                .build();
        NotificationDto notificationDto1 = new NotificationDto(notification);

        TravelNotification travelNotification = TravelNotification.builder()
                .receiverNumber(1)
                .title("여행 알림")
                .content("내용")
                .createdAt(LocalDateTime.of(2024, 11, 19, 0, 0))
                .isRead(true)
                .travelNumber(1)
                .travelTitle("여행 제목")
                .travelDueDate(LocalDate.of(2024, 11, 21))
                .travelHost(true)
                .build();
        NotificationDto notificationDto2 = new TravelNotificationDto(travelNotification);

        List<NotificationDto> notifications = Arrays.asList(notificationDto1, notificationDto2);
        Page<NotificationDto> result = new PageImpl<>(notifications, PageRequest.of(0, 5), notifications.size());

        given(notificationService.getNotificationsByUser(any(PageRequest.class), anyInt()))
                .willReturn(result);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/notifications"));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content.size()").value(2))
                .andExpect(jsonPath("$.success.content[0].title").value("알림"))
                .andExpect(jsonPath("$.success.content[0].content").value("내용"))
                .andExpect(jsonPath("$.success.content[0].createdAt").value("2024-11-20 00:00"))
                .andExpect(jsonPath("$.success.content[0].isRead").value(false))
                .andExpect(jsonPath("$.success.content[1].title").value("여행 알림"))
                .andExpect(jsonPath("$.success.content[1].content").value("내용"))
                .andExpect(jsonPath("$.success.content[1].createdAt").value("2024-11-19 00:00"))
                .andExpect(jsonPath("$.success.content[1].isRead").value(true))
                .andExpect(jsonPath("$.success.content[1].travelNumber").value(1))
                .andExpect(jsonPath("$.success.content[1].travelTitle").value("여행 제목"))
                .andExpect(jsonPath("$.success.content[1].travelDueDate").value("2024-11-21"))
                .andExpect(jsonPath("$.success.content[1].travelHostUser").value(true));
    }

}