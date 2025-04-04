package swyp.swyp6_team7.travel.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
class TravelControllerTest extends IntegrationTest {

    private Users user;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        user = createUser("test", "password");
        System.out.println("User: " + user.getUserEmail());
        LoginTokenResponse response = login("test@test.com", "password");
        accessToken = response.getAccessToken();
        System.out.println(accessToken);
    }

    @DisplayName("create: 사용자는 여행 콘텐츠를 생성할 수 있다.")
    @Transactional
    @Test
    public void create() throws Exception {
        // given
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("서울")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusWeeks(1))
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType("모두")
                .periodType("일주일 이하")
                .tags(List.of("쇼핑"))
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.travelNumber").value(1));
    }

    @DisplayName("create: 여행 생성 요청 시 title 길이가 20자를 넘으면 예외가 발생한다.")
    @Test
    @Transactional
    public void createWithValidateTitleLength() throws Exception {
        // given
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("서울")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusWeeks(1))
                .title("*".repeat(21))
                .details("여행 내용")
                .maxPerson(2)
                .genderType("모두")
                .periodType("일주일 이하")
                .tags(List.of("쇼핑"))
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.reason").value("여행 제목은 최대 20자 입니다."));
    }

    @DisplayName("create: 여행 생성 요청 시 maxPerson의 값이 0보다 작으면 예외가 발생한다.")
    @Test
    @Transactional
    public void createWithValidateMaxPerson() throws Exception {
        // given
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("서울")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusWeeks(1))
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(-1)
                .genderType("모두")
                .periodType("일주일 이하")
                .tags(List.of("쇼핑"))
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.reason").value("여행 참가 최대 인원은 0보다 작을 수 없습니다."));
    }

    @DisplayName("getDetailsByNumber: 비회원(비로그인) 사용자는 여행 상세 정보를 단건 조회할 수 있다.")
    @Test
    @Transactional
    public void getDetailsByNumberWhenNonMember() throws Exception {
        // given
        int travelNumber = 1;
        int hostNumber = 1;
        TravelDetailResponse travelDetailResponse = createDetailResponse(travelNumber, hostNumber);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travel/detail/{travelNumber}", travelNumber)
                .header("Authorization", "Bearer " + accessToken)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.travelNumber").value(10))
                .andExpect(jsonPath("$.success.userNumber").value(1))
                .andExpect(jsonPath("$.success.userName").value("주최자명"))
                .andExpect(jsonPath("$.success.userAgeGroup").value("20대"))
                .andExpect(jsonPath("$.success.profileUrl").value("https://user-profile-url"))
                .andExpect(jsonPath("$.success.createdAt").value("2024-12-06 12:00"))
                .andExpect(jsonPath("$.success.location").value("서울"))
                .andExpect(jsonPath("$.success.startDate").value("2024-11-22"))
                .andExpect(jsonPath("$.success.endDate").value("2024-11-28"))
                .andExpect(jsonPath("$.success.title").value("여행 제목"))
                .andExpect(jsonPath("$.success.details").value("여행 상세 내용"))
                .andExpect(jsonPath("$.success.viewCount").value(10))
                .andExpect(jsonPath("$.success.enrollCount").value(2))
                .andExpect(jsonPath("$.success.bookmarkCount").value(5))
                .andExpect(jsonPath("$.success.nowPerson").value(1))
                .andExpect(jsonPath("$.success.maxPerson").value(4))
                .andExpect(jsonPath("$.success.genderType").value("모두"))
                .andExpect(jsonPath("$.success.tags[0]").value("자연"))
                .andExpect(jsonPath("$.success.tags[1]").value("쇼핑"))
                .andExpect(jsonPath("$.success.postStatus").value("진행중"))
                .andExpect(jsonPath("$.success.loginMemberRelatedInfo").value(nullValue()));
    }

    @DisplayName("getDetailsByNumber: 로그인 사용자는 여행 상세 단건 정보와 자신과 관련된 추가 정보를 함께 조회할 수 있다.")
    @Transactional
    @Test
    public void getDetailsByNumberWhenMember() throws Exception {
        // given
        int travelNumber = 1;
        int hostNumber = 1;
        // when
        ResultActions resultActions = mockMvc.perform(get("/api/travel/detail/{travelNumber}", travelNumber)
                .header("Authorization", "Bearer " + accessToken));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.travelNumber").value(10))
                .andExpect(jsonPath("$.success.userNumber").value(1))
                .andExpect(jsonPath("$.success.userName").value("주최자명"))
                .andExpect(jsonPath("$.success.userAgeGroup").value("20대"))
                .andExpect(jsonPath("$.success.profileUrl").value("https://user-profile-url"))
                .andExpect(jsonPath("$.success.createdAt").value("2024-12-06 12:00"))
                .andExpect(jsonPath("$.success.location").value("서울"))
                .andExpect(jsonPath("$.success.startDate").value("2024-11-22"))
                .andExpect(jsonPath("$.success.endDate").value("2024-11-28"))
                .andExpect(jsonPath("$.success.title").value("여행 제목"))
                .andExpect(jsonPath("$.success.details").value("여행 상세 내용"))
                .andExpect(jsonPath("$.success.viewCount").value(10))
                .andExpect(jsonPath("$.success.enrollCount").value(2))
                .andExpect(jsonPath("$.success.bookmarkCount").value(5))
                .andExpect(jsonPath("$.success.nowPerson").value(1))
                .andExpect(jsonPath("$.success.maxPerson").value(4))
                .andExpect(jsonPath("$.success.genderType").value("모두"))
                .andExpect(jsonPath("$.success.tags[0]").value("자연"))
                .andExpect(jsonPath("$.success.tags[1]").value("쇼핑"))
                .andExpect(jsonPath("$.success.postStatus").value("진행중"))
                .andExpect(jsonPath("$.success.loginMemberRelatedInfo.hostUser").value(false))
                .andExpect(jsonPath("$.success.loginMemberRelatedInfo.enrollmentNumber").value(5L))
                .andExpect(jsonPath("$.success.loginMemberRelatedInfo.bookmarked").value(true));
    }

    @DisplayName("update: 사용자는 자신이 작성한 여행 콘텐츠를 수정할 수 있다.")
    @Transactional
    @Test
    public void update() throws Exception {
        // given
        TravelUpdateRequest request = TravelUpdateRequest.builder()
                .locationName("서울")
                .startDate(LocalDate.of(2024, 12, 22))
                .endDate(LocalDate.of(2024, 12, 28))
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType("모두")
                .periodType("일주일 이하")
                .tags(List.of("쇼핑"))
                .build();

        int travelNumber = 10;
        Travel updatedTravel = createTravel(travelNumber, 2);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.travelNumber").value(10));
    }

    private Travel createTravel(int travelNumber, int hostNumber) {
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
                .startDate(LocalDate.of(2024, 11, 22))
                .endDate(LocalDate.of(2024, 11, 28))
                .title("여행 제목")
                .details("여행 상세 내용")
                .viewCount(10)
                .enrollCount(2)
                .bookmarkCount(5)
                .nowPerson(1)
                .maxPerson(4)
                .genderType(GenderType.MIXED.getDescription())
                .periodType(PeriodType.ONE_WEEK.getDescription())
                .tags(List.of("자연", "쇼핑"))
                .postStatus(TravelStatus.IN_PROGRESS.toString())
                .build();
    }
}
