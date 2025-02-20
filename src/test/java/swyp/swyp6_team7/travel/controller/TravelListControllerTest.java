package swyp.swyp6_team7.travel.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.global.utils.auth.MemberAuthorizeUtil;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelListService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TravelListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TravelListService travelListService;

    private MockedStatic<MemberAuthorizeUtil> mockedStaticMemberAuthorizeUtil;

    @AfterEach
    void tearDown() {
        // Reset static mock after each test to avoid interference
        if (mockedStaticMemberAuthorizeUtil != null) {
            mockedStaticMemberAuthorizeUtil.close();
        }
    }

    @DisplayName("내가 만든 여행 게시글 목록 조회 테스트")
    @WithMockUser
    @Test
    void getMyCreatedTravels_ShouldReturnListOfCreatedTravels() throws Exception {
        // given
        String token = "Bearer test-token";
        //Integer userNumber = 1;
        Pageable pageable = PageRequest.of(0, 5);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime createdAt = LocalDateTime.parse("2024-10-02 21:56", dateTimeFormatter);
        LocalDate registerDue = LocalDate.parse("2025-05-15", dateFormatter);
        TravelListResponseDto responseDto = TravelListResponseDto.builder()
                .travelNumber(25)
                .title("호주 여행 같이 갈 사람 구해요")
                .userNumber(3)
                .userName("김모잉")
                .tags(Collections.singletonList("즉흥"))
                .nowPerson(1)
                .maxPerson(5)
                .createdAt(createdAt)
                .registerDue(registerDue)
                .isBookmarked(true)
                .build();
        Page<TravelListResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto), pageable, 1);

        // when
        mockedStaticMemberAuthorizeUtil = mockStatic(MemberAuthorizeUtil.class);
        when(MemberAuthorizeUtil.getLoginUserNumber()).thenReturn(2);

        when(travelListService.getTravelListByUser(2, pageable)).thenReturn(page);

        // then
        mockMvc.perform(get("/api/my-travels")
//                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content[0].travelNumber").value(25))
                .andExpect(jsonPath("$.success.content[0].title").value("호주 여행 같이 갈 사람 구해요"))
                .andExpect(jsonPath("$.success.content[0].userNumber").value(3))
                .andExpect(jsonPath("$.success.content[0].userName").value("김모잉"))
                .andExpect(jsonPath("$.success.content[0].tags[0]").value("즉흥"))
                .andExpect(jsonPath("$.success.content[0].nowPerson").value(1))
                .andExpect(jsonPath("$.success.content[0].maxPerson").value(5))
                .andExpect(jsonPath("$.success.content[0].createdAt").value("2024-10-02 21:56"))
                .andExpect(jsonPath("$.success.content[0].registerDue").value("2025-05-15"))
                //.andExpect(jsonPath("$.content[0].isBookmarked").value(true))
                .andExpect(jsonPath("$.success.page.size").value(5))
                .andExpect(jsonPath("$.success.page.number").value(0))
                .andExpect(jsonPath("$.success.page.totalElements").value(1))
                .andExpect(jsonPath("$.success.page.totalPages").value(1));
    }
}
