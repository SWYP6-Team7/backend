package swyp.swyp6_team7.travel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TravelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    TravelRepository travelRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LocationRepository locationRepository;

    Users user;


    @BeforeEach
    void setSecurityContext() {
        user = userRepository.save(Users.builder()
                .userNumber(1)
                .userEmail("abc@test.com")
                .userPw("1234")
                .userName("username")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .role(UserRole.USER)
                .userRegDate(LocalDateTime.now())
                .userStatus(UserStatus.ABLE)
                .build());

        var userDetails = userDetailsService.loadUserByUsername(String.valueOf(user.getUserNumber()));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        travelRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("create: 사용자는 여행 콘텐츠를 생성할 수 있다")
    @Test
    public void create() throws Exception {
        // given
        Location travelLocation = Location.builder()
                .locationName("서울")
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);

        LocalDate dueDate = LocalDate.now().plusDays(1);
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("Seoul")
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType(GenderType.MIXED.toString())
                .dueDate(dueDate)
                .periodType(PeriodType.ONE_WEEK.toString())
                .tags(List.of())
                .completionStatus(true)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/travel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.userNumber").value(user.getUserNumber()))
                .andExpect(jsonPath("$.location").value("Seoul"))
                .andExpect(jsonPath("$.title").value("여행 제목"))
                .andExpect(jsonPath("$.details").value("여행 내용"))
                .andExpect(jsonPath("$.dueDate").value(dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .andExpect(jsonPath("$.postStatus").value(TravelStatus.IN_PROGRESS.toString()));
    }


    @DisplayName("getDetailsByNumber: 여행 콘텐츠 단건 상세 정보 조회에 성공한다")
    @Test
    //@DirtiesContext
    public void getDetailsByNumber() throws Exception {
        // given
        String url = "/api/travel/detail/{travelNumber}";
        Travel savedTravel = createTravel(user.getUserNumber(), TravelStatus.IN_PROGRESS);
        Location travelLocation = Location.builder()
                .locationName("Jeju-" + UUID.randomUUID().toString())
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);

        // when
        ResultActions resultActions = mockMvc.perform(get(url, savedTravel.getNumber()));


        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(savedTravel.getTitle()))
                .andExpect(jsonPath("$.userNumber").value(user.getUserNumber()));
    }

   /* @DisplayName("getDetailsByNumber: 작성자가 아닌 경우 Draft 상태의 콘텐츠 단건 조회를 하면 예외가 발생")
    @Test
    @DirtiesContext
    public void getDetailsByNumberDraftException() throws Exception {
        // given
        String url = "/api/travel/detail/{travelNumber}";
        Travel savedTravel = createTravel(2, TravelStatus.DRAFT);
        Location travelLocation = Location.builder()
                .locationName("Seoul-"+System.currentTimeMillis())
                .locationType(LocationType.DOMESTIC)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(get(url, savedTravel.getNumber(), travelLocation));

        // then
        resultActions
                .andExpect(status().is5xxServerError());
    }*/

    @DisplayName("getDetailsByNumber: Deleted 상태의 콘텐츠 단건 조회를 하면 예외가 발생")
    @Test
    //@DirtiesContext
    public void getDetailsByNumberDeletedException() throws Exception {
        // given
        String url = "/api/travel/detail/{travelNumber}";
        Travel savedTravel = createTravel(user.getUserNumber(), TravelStatus.DELETED);
        Location travelLocation = Location.builder()
                .locationName("Seoul" + UUID.randomUUID().toString())
                .locationType(LocationType.DOMESTIC)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(get(url, savedTravel.getNumber(), travelLocation));

        // then
        resultActions
                .andExpect(status().is5xxServerError());
    }

    private Travel createTravel(int userNumber, TravelStatus status) {
        Location travelLocation = Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        return travelRepository.save(Travel.builder()
                .title("Travel Controller")
                .location(travelLocation)
                .userNumber(userNumber)
                .genderType(GenderType.NONE)
                .periodType(PeriodType.NONE)
                .status(status)
                .build()
        );
    }
}