package swyp.swyp6_team7.travel.controller;

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
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.mock.WithMockCustomUser;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.TravelDetailLoginMemberRelatedDto;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.service.TravelService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TravelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TravelService travelService;


    @DisplayName("create: 사용자는 여행 콘텐츠를 생성할 수 있다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    public void create() throws Exception {
        // given
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("서울")
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType("모두")
                .dueDate(LocalDate.now().plusDays(10))
                .periodType("일주일 이하")
                .tags(List.of("쇼핑"))
                .completionStatus(true)
                .build();

        int travelNumber = 10;
        Travel createdTravel = createTravel(travelNumber, 2);

        given(travelService.create(any(TravelCreateRequest.class), anyInt()))
                .willReturn(createdTravel);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.travelNumber").value(10));
        then(travelService.create(any(TravelCreateRequest.class), eq(2)));
    }

    @DisplayName("create: 여행 생성 요청 시 title 길이가 20자를 넘으면 예외가 발생한다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    public void createWithValidateTitleLength() throws Exception {
        // given
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("서울")
                .title("*".repeat(21))
                .details("여행 내용")
                .maxPerson(2)
                .genderType("모두")
                .dueDate(LocalDate.now().plusDays(10))
                .periodType("일주일 이하")
                .tags(List.of("쇼핑"))
                .completionStatus(true)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("여행 제목은 최대 20자 입니다."));
        then(travelService.create(any(TravelCreateRequest.class), eq(2)));
    }

    @DisplayName("create: 여행 생성 요청 시 maxPerson의 값이 0보다 작을 경우 예외가 발생한다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    public void createWithValidateMaxPerson() throws Exception {
        // given
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("서울")
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(-1)
                .genderType("모두")
                .dueDate(LocalDate.now().plusDays(10))
                .periodType("일주일 이하")
                .tags(List.of("쇼핑"))
                .completionStatus(true)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("여행 참가 최대 인원은 0보다 작을 수 없습니다."));
        then(travelService.create(any(TravelCreateRequest.class), eq(2)));
    }

    @DisplayName("create: 여행 생성 요청 시 dueDate가 오늘보다 이전인 경우 예외가 발생한다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    public void createWithValidateDueDate() throws Exception {
        // given
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("서울")
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType("모두")
                .dueDate(LocalDate.now().minusDays(10))
                .periodType("일주일 이하")
                .tags(List.of("쇼핑"))
                .completionStatus(true)
                .build();

        int travelNumber = 10;
        Travel createdTravel = createTravel(travelNumber, 2);

        given(travelService.create(any(TravelCreateRequest.class), anyInt()))
                .willReturn(createdTravel);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().string("여행 신청 마감 날짜는 현재 날짜보다 이전일 수 없습니다."));
        then(travelService.create(any(TravelCreateRequest.class), eq(2)));
    }


    @DisplayName("getDetailsByNumber: 비회원(비로그인) 사용자는 여행 상세 정보를 단건 조회할 수 있다.")
    @Test
    public void getDetailsByNumberWhenNonMember() throws Exception {
        // given
        int travelNumber = 10;
        int hostNumber = 1;
        TravelDetailResponse travelDetailResponse = createDetailResponse(travelNumber, hostNumber);

        given(travelService.getDetailsByNumber(anyInt()))
                .willReturn(travelDetailResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travel/detail/{travelNumber}", travelNumber));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelNumber").value(10))
                .andExpect(jsonPath("$.userNumber").value(1))
                .andExpect(jsonPath("$.userName").value("주최자명"))
                .andExpect(jsonPath("$.userAgeGroup").value("20대"))
                .andExpect(jsonPath("$.profileUrl").value("https://user-profile-url"))
                .andExpect(jsonPath("$.createdAt").value("2024-12-06 12:00"))
                .andExpect(jsonPath("$.location").value("서울"))
                .andExpect(jsonPath("$.title").value("여행 제목"))
                .andExpect(jsonPath("$.details").value("여행 상세 내용"))
                .andExpect(jsonPath("$.viewCount").value(10))
                .andExpect(jsonPath("$.enrollCount").value(2))
                .andExpect(jsonPath("$.bookmarkCount").value(5))
                .andExpect(jsonPath("$.nowPerson").value(1))
                .andExpect(jsonPath("$.maxPerson").value(4))
                .andExpect(jsonPath("$.genderType").value("모두"))
                .andExpect(jsonPath("$.dueDate").value("2024-12-30"))
                .andExpect(jsonPath("$.tags[0]").value("자연"))
                .andExpect(jsonPath("$.tags[1]").value("쇼핑"))
                .andExpect(jsonPath("$.postStatus").value("진행중"))
                .andExpect(jsonPath("$.loginMemberRelatedInfo").value(nullValue()));
        then(travelService).should().getDetailsByNumber(travelNumber);
    }

    @DisplayName("getDetailsByNumber: 로그인 사용자는 여행 상세 단건 정보와 자신과 관련된 추가 정보를 함께 조회할 수 있다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    public void getDetailsByNumberWhenMember() throws Exception {
        // given
        int travelNumber = 10;
        int hostNumber = 1;
        TravelDetailResponse travelDetailResponse = createDetailResponse(travelNumber, hostNumber);
        TravelDetailLoginMemberRelatedDto memberRelatedDto = TravelDetailLoginMemberRelatedDto.builder()
                .isHostUser(false)
                .enrollmentNumber(5L)
                .isBookmarked(true)
                .build();

        given(travelService.getDetailsByNumber(anyInt()))
                .willReturn(travelDetailResponse);
        given(travelService.getTravelDetailMemberRelatedInfo(anyInt(), anyInt(), anyInt(), anyString()))
                .willReturn(memberRelatedDto);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travel/detail/{travelNumber}", travelNumber));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelNumber").value(10))
                .andExpect(jsonPath("$.userNumber").value(1))
                .andExpect(jsonPath("$.userName").value("주최자명"))
                .andExpect(jsonPath("$.userAgeGroup").value("20대"))
                .andExpect(jsonPath("$.profileUrl").value("https://user-profile-url"))
                .andExpect(jsonPath("$.createdAt").value("2024-12-06 12:00"))
                .andExpect(jsonPath("$.location").value("서울"))
                .andExpect(jsonPath("$.title").value("여행 제목"))
                .andExpect(jsonPath("$.details").value("여행 상세 내용"))
                .andExpect(jsonPath("$.viewCount").value(10))
                .andExpect(jsonPath("$.enrollCount").value(2))
                .andExpect(jsonPath("$.bookmarkCount").value(5))
                .andExpect(jsonPath("$.nowPerson").value(1))
                .andExpect(jsonPath("$.maxPerson").value(4))
                .andExpect(jsonPath("$.genderType").value("모두"))
                .andExpect(jsonPath("$.dueDate").value("2024-12-30"))
                .andExpect(jsonPath("$.tags[0]").value("자연"))
                .andExpect(jsonPath("$.tags[1]").value("쇼핑"))
                .andExpect(jsonPath("$.postStatus").value("진행중"))
                .andExpect(jsonPath("$.loginMemberRelatedInfo.hostUser").value(false))
                .andExpect(jsonPath("$.loginMemberRelatedInfo.enrollmentNumber").value(5L))
                .andExpect(jsonPath("$.loginMemberRelatedInfo.bookmarked").value(true));
        then(travelService).should().getDetailsByNumber(travelNumber);
        then(travelService).should().getTravelDetailMemberRelatedInfo(2, 10, 1, "진행중");
    }

    private Travel createTravel(int travelNumber, int hostNumber){
        return Travel.builder()
                .number(travelNumber)
                .userNumber(hostNumber)
                .build();
    }

    private TravelDetailResponse createDetailResponse(int travelNumber, int userNumber) {
        return TravelDetailResponse.builder()
                .travelNumber(travelNumber)
                .userNumber(userNumber)
                .userName("주최자명")
                .userAgeGroup(AgeGroup.TWENTY.getValue())
                .profileUrl("https://user-profile-url")
                .createdAt(LocalDateTime.of(2024, 12, 06, 12, 0))
                .location("서울")
                .title("여행 제목")
                .details("여행 상세 내용")
                .viewCount(10)
                .enrollCount(2)
                .bookmarkCount(5)
                .nowPerson(1)
                .maxPerson(4)
                .genderType(GenderType.MIXED.getDescription())
                .dueDate(LocalDate.of(2024, 12, 30))
                .periodType(PeriodType.ONE_WEEK.getDescription())
                .tags(List.of("자연", "쇼핑"))
                .postStatus(TravelStatus.IN_PROGRESS.toString())
                .build();
    }
}