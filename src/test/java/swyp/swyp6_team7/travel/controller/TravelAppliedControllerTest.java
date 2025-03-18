package swyp.swyp6_team7.travel.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.global.utils.auth.MemberAuthorizeUtil;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelAppliedService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TravelAppliedControllerTest extends IntegrationTest {

    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String BEARER_TOKEN = "Bearer test-token";
    @MockBean
    private TravelAppliedService travelAppliedService;

    @DisplayName("사용자가 신청한 여행 목록을 조회한다")
    @WithMockUser
    @Test
    void getAppliedTrips_ShouldReturnListOfAppliedTrips() throws Exception {
        // given
        String token = "Bearer test-token";
        Integer userNumber = 1;
        Pageable pageable = PageRequest.of(0, 5);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDateTime createdAt = LocalDateTime.parse("2024-10-02 21:56", dateTimeFormatter);

        TravelListResponseDto responseDto = TravelListResponseDto.builder()
                .travelNumber(25)
                .title("호주 여행 같이 갈 사람 구해요")
                .userNumber(3)
                .userName("김모잉")
                .tags(Collections.singletonList("즉흥"))
                .nowPerson(1)
                .maxPerson(5)
                .createdAt(createdAt)
                .isBookmarked(false)
                .build();
        Page<TravelListResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto), pageable, 1);

        try (MockedStatic<MemberAuthorizeUtil> mockedStatic = mockStatic(MemberAuthorizeUtil.class)) {
            mockedStatic.when(MemberAuthorizeUtil::getLoginUserNumber).thenReturn(userNumber);

            when(travelAppliedService.getAppliedTripsByUser(userNumber, pageable)).thenReturn(page);

            // then
            mockMvc.perform(get("/api/my-applied-travels")
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success.content[0].travelNumber").value(25))
                    .andExpect(jsonPath("$.success.content[0].title").value("호주 여행 같이 갈 사람 구해요"))
                    .andExpect(jsonPath("$.success.content[0].userName").value("김모잉"))
                    .andExpect(jsonPath("$.success.content[0].tags[0]").value("즉흥"))
                    .andExpect(jsonPath("$.success.page.size").value(5))
                    .andExpect(jsonPath("$.success.page.number").value(0))
                    .andExpect(jsonPath("$.success.page.totalElements").value(1))
                    .andExpect(jsonPath("$.success.page.totalPages").value(1));
        }
    }

    @DisplayName("사용자가 특정 여행에 대한 참가 신청을 취소한다")
    @WithMockUser
    @Test
    void cancelTripApplication_ShouldCancelApplication() throws Exception {
        // given
        int userNumber = 1;
        int travelNumber = 2;

        try (MockedStatic<MemberAuthorizeUtil> mockedStatic = mockStatic(MemberAuthorizeUtil.class)) {
            mockedStatic.when(MemberAuthorizeUtil::getLoginUserNumber).thenReturn(userNumber);

            // Do nothing when canceling the application
            Mockito.doNothing().when(travelAppliedService).cancelApplication(userNumber, travelNumber);

            // when & then
            mockMvc.perform(delete("/api/my-applied-travels/{travelNumber}/cancel", travelNumber)
                            .header(AUTHORIZATION_HEADER, BEARER_TOKEN))
                    .andExpect(status().isOk());
        }
    }
}
