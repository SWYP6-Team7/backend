package swyp.swyp6_team7.travel.controller;

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
import swyp.swyp6_team7.travel.dto.TravelRecommendForMemberDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendForNonMemberDto;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.service.TravelHomeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TravelHomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TravelHomeService travelHomeService;


    @DisplayName("getRecentlyCreatedTravels: 최근 생성된 여행 순서로 여행 목록을 조회할 수 있다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    void getRecentlyCreatedTravels() throws Exception {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 12, 5, 12, 0);
        TravelRecentDto travel1 = createRecentDto(10, 1, createdAt, true);
        TravelRecentDto travel2 = createRecentDto(11, 2, createdAt.plusDays(1), false);
        List<TravelRecentDto> travels = Arrays.asList(travel2, travel1);
        Page<TravelRecentDto> result = new PageImpl<>(travels, PageRequest.of(0, 5), travels.size());

        given(travelHomeService.getTravelsSortedByCreatedAt(any(PageRequest.class), any(Integer.class)))
                .willReturn(result);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travels/recent"));

        // then
        then(travelHomeService).should(times(1)).getTravelsSortedByCreatedAt(PageRequest.of(0, 5), 2);
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content.size()").value(2))
                .andExpect(jsonPath("$.success.content[0].travelNumber").value(11))
                .andExpect(jsonPath("$.success.content[0].title").value("여행 제목"))
                .andExpect(jsonPath("$.success.content[0].location").value("서울"))
                .andExpect(jsonPath("$.success.content[0].userNumber").value(2))
                .andExpect(jsonPath("$.success.content[0].userName").value("주최자명"))
                .andExpect(jsonPath("$.success.content[0].tags").isEmpty())
                .andExpect(jsonPath("$.success.content[0].nowPerson").value(1))
                .andExpect(jsonPath("$.success.content[0].maxPerson").value(3))
                .andExpect(jsonPath("$.success.content[0].createdAt").value("2024-12-06 12:00"))
                .andExpect(jsonPath("$.success.content[0].registerDue").value("2024-12-30"))
                .andExpect(jsonPath("$.success.content[0].bookmarked").value(false))
                .andExpect(jsonPath("$.success.content[1].travelNumber").value(10))
                .andExpect(jsonPath("$.success.content[1].userNumber").value(1))
                .andExpect(jsonPath("$.success.content[1].createdAt").value("2024-12-05 12:00"))
                .andExpect(jsonPath("$.success.content[1].bookmarked").value(true));
    }

    @DisplayName("getRecentlyCreatedTravels: 비로그인 사용자도 최근 생성된 여행 순서로 여행 목록을 조회할 수 있다.")
    @Test
    void getRecentlyCreatedTravelsWhenNotLogin() throws Exception {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 12, 5, 12, 0);
        TravelRecentDto travel1 = createRecentDto(10, 1, createdAt, false);
        TravelRecentDto travel2 = createRecentDto(11, 2, createdAt.plusDays(1), false);
        List<TravelRecentDto> travels = Arrays.asList(travel2, travel1);
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<TravelRecentDto> result = new PageImpl<>(travels, pageRequest, travels.size());

        given(travelHomeService.getTravelsSortedByCreatedAt(pageRequest, null))
                .willReturn(result);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travels/recent"));

        // then
        then(travelHomeService).should(times(1)).getTravelsSortedByCreatedAt(PageRequest.of(0, 5), null);
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content.size()").value(2))
                .andExpect(jsonPath("$.success.content[0].travelNumber").value(11))
                .andExpect(jsonPath("$.success.content[0].title").value("여행 제목"))
                .andExpect(jsonPath("$.success.content[0].location").value("서울"))
                .andExpect(jsonPath("$.success.content[0].userNumber").value(2))
                .andExpect(jsonPath("$.success.content[0].userName").value("주최자명"))
                .andExpect(jsonPath("$.success.content[0].tags").isEmpty())
                .andExpect(jsonPath("$.success.content[0].nowPerson").value(1))
                .andExpect(jsonPath("$.success.content[0].maxPerson").value(3))
                .andExpect(jsonPath("$.success.content[0].createdAt").value("2024-12-06 12:00"))
                .andExpect(jsonPath("$.success.content[0].registerDue").value("2024-12-30"))
                .andExpect(jsonPath("$.success.content[0].bookmarked").value(false))
                .andExpect(jsonPath("$.success.content[1].travelNumber").value(10))
                .andExpect(jsonPath("$.success.content[1].userNumber").value(1))
                .andExpect(jsonPath("$.success.content[1].createdAt").value("2024-12-05 12:00"))
                .andExpect(jsonPath("$.success.content[1].bookmarked").value(false));
    }

    @DisplayName("getRecommendTravels: 로그인 사용자는 추천 여행 목록을 조회할 수 있다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    void getRecommendTravelsWhenLogin() throws Exception {
        // given
        TravelRecommendForMemberDto travel1 = createdRecommendDto(10, 1, List.of(), 0, false);
        TravelRecommendForMemberDto travel2 = createdRecommendDto(11, 2, List.of("자연", "쇼핑"), 2, true);
        List<TravelRecommendForMemberDto> travels = Arrays.asList(travel2, travel1);

        Page<TravelRecommendForMemberDto> result = new PageImpl<>(travels, PageRequest.of(0, 5), travels.size());
        given(travelHomeService.getRecommendTravelsByMember(any(PageRequest.class), anyInt(), any(LocalDate.class)))
                .willReturn(result);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travels/recommend"));

        // then
        then(travelHomeService).should(times(1))
                .getRecommendTravelsByMember(any(PageRequest.class), anyInt(), any(LocalDate.class));
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content.size()").value(2))
                .andExpect(jsonPath("$.success.content[0].travelNumber").value(11))
                .andExpect(jsonPath("$.success.content[0].title").value("여행 제목"))
                .andExpect(jsonPath("$.success.content[0].location").value("서울"))
                .andExpect(jsonPath("$.success.content[0].userNumber").value(2))
                .andExpect(jsonPath("$.success.content[0].userName").value("주최자명"))
                .andExpect(jsonPath("$.success.content[0].tags[0]").value("자연"))
                .andExpect(jsonPath("$.success.content[0].tags[1]").value("쇼핑"))
                .andExpect(jsonPath("$.success.content[0].nowPerson").value(1))
                .andExpect(jsonPath("$.success.content[0].maxPerson").value(3))
                .andExpect(jsonPath("$.success.content[0].createdAt").value("2024-12-05 12:00"))
                .andExpect(jsonPath("$.success.content[0].registerDue").value("2024-12-30"))
                .andExpect(jsonPath("$.success.content[0].bookmarked").value(true))
                .andExpect(jsonPath("$.success.content[1].travelNumber").value(10))
                .andExpect(jsonPath("$.success.content[1].title").value("여행 제목"))
                .andExpect(jsonPath("$.success.content[1].location").value("서울"))
                .andExpect(jsonPath("$.success.content[1].userNumber").value(1))
                .andExpect(jsonPath("$.success.content[1].userName").value("주최자명"))
                .andExpect(jsonPath("$.success.content[1].tags").isEmpty())
                .andExpect(jsonPath("$.success.content[1].nowPerson").value(1))
                .andExpect(jsonPath("$.success.content[1].maxPerson").value(3))
                .andExpect(jsonPath("$.success.content[1].createdAt").value("2024-12-05 12:00"))
                .andExpect(jsonPath("$.success.content[1].registerDue").value("2024-12-30"))
                .andExpect(jsonPath("$.success.content[1].bookmarked").value(false));
    }

    @DisplayName("getRecommendTravels: 비로그인 사용자는 추천 여행 목록을 조회할 수 있다.")
    @Test
    void getRecommendTravelsWhenNotLogin() throws Exception {
        // given
        TravelRecommendForNonMemberDto travel1 = createRecommendForNonMemberDto(10, 1);
        TravelRecommendForNonMemberDto travel2 = createRecommendForNonMemberDto(11, 2);
        List<TravelRecommendForNonMemberDto> travels = Arrays.asList(travel2, travel1);

        Page<TravelRecommendForNonMemberDto> result = new PageImpl<>(travels, PageRequest.of(0, 5), travels.size());
        given(travelHomeService.getRecommendTravelsByNonMember(any(PageRequest.class), any(LocalDate.class)))
                .willReturn(result);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travels/recommend"));

        // then
        then(travelHomeService).should(times(1))
                .getRecommendTravelsByNonMember(any(PageRequest.class), any(LocalDate.class));
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content.size()").value(2))
                .andExpect(jsonPath("$.success.content[0].travelNumber").value(11))
                .andExpect(jsonPath("$.success.content[0].title").value("여행 제목"))
                .andExpect(jsonPath("$.success.content[0].location").value("서울"))
                .andExpect(jsonPath("$.success.content[0].userNumber").value(2))
                .andExpect(jsonPath("$.success.content[0].userName").value("주최자명"))
                .andExpect(jsonPath("$.success.content[0].tags").isEmpty())
                .andExpect(jsonPath("$.success.content[0].nowPerson").value(1))
                .andExpect(jsonPath("$.success.content[0].maxPerson").value(3))
                .andExpect(jsonPath("$.success.content[0].createdAt").value("2024-12-05 12:00"))
                .andExpect(jsonPath("$.success.content[0].registerDue").value("2024-12-30"))
                .andExpect(jsonPath("$.success.content[0].bookmarked").value(false))
                .andExpect(jsonPath("$.success.content[1].travelNumber").value(10))
                .andExpect(jsonPath("$.success.content[1].title").value("여행 제목"))
                .andExpect(jsonPath("$.success.content[1].location").value("서울"))
                .andExpect(jsonPath("$.success.content[1].userNumber").value(1))
                .andExpect(jsonPath("$.success.content[1].userName").value("주최자명"))
                .andExpect(jsonPath("$.success.content[1].tags").isEmpty())
                .andExpect(jsonPath("$.success.content[1].nowPerson").value(1))
                .andExpect(jsonPath("$.success.content[1].maxPerson").value(3))
                .andExpect(jsonPath("$.success.content[1].createdAt").value("2024-12-05 12:00"))
                .andExpect(jsonPath("$.success.content[1].registerDue").value("2024-12-30"))
                .andExpect(jsonPath("$.success.content[1].bookmarked").value(false));
    }

    private TravelRecentDto createRecentDto(int travelNumber, int userNumber, LocalDateTime createdAt, boolean bookmarked) {
        return TravelRecentDto.builder()
                .travelNumber(travelNumber)
                .title("여행 제목")
                .location("서울")
                .userName("주최자명")
                .userNumber(userNumber)
                .tags(List.of())
                .nowPerson(1)
                .maxPerson(3)
                .createdAt(createdAt)
                .registerDue(LocalDate.of(2024, 12, 30))
                .isBookmarked(bookmarked)
                .build();
    }

    private TravelRecommendForMemberDto createdRecommendDto(int travelNumber, int userNumber, List<String> tags, int preferredNumber, boolean bookmarked) {
        return TravelRecommendForMemberDto.builder()
                .travelNumber(travelNumber)
                .title("여행 제목")
                .location("서울")
                .userName("주최자명")
                .userNumber(userNumber)
                .tags(tags)
                .nowPerson(1)
                .maxPerson(3)
                .createdAt(LocalDateTime.of(2024, 12, 5, 12, 0))
                .registerDue(LocalDate.of(2024, 12, 30))
                .preferredNumber(preferredNumber)
                .isBookmarked(bookmarked)
                .build();
    }

    private TravelRecommendForNonMemberDto createRecommendForNonMemberDto(int travelNumber, int userNumber) {
        return TravelRecommendForNonMemberDto.builder()
                .travelNumber(travelNumber)
                .title("여행 제목")
                .location("서울")
                .userName("주최자명")
                .userNumber(userNumber)
                .tags(List.of())
                .nowPerson(1)
                .maxPerson(3)
                .createdAt(LocalDateTime.of(2024, 12, 5, 12, 0))
                .registerDue(LocalDate.of(2024, 12, 30))
                .build();
    }
}