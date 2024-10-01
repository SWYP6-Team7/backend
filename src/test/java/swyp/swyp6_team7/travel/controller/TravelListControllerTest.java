package swyp.swyp6_team7.travel.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelListService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @MockBean
    private JwtProvider jwtProvider;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer test-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("내가 만든 여행 게시글 목록 조회 테스트")
    @WithMockUser
    void getMyCreatedTravels() throws Exception {
        // given
        int userNumber = 1;

        // Mock the JwtProvider to return the userNumber from the token
        when(jwtProvider.getUserNumber("test-token")).thenReturn(userNumber);

        // Mock the TravelListService to return a list of travels
        List<TravelListResponseDto> mockTravelList = List.of(
                new TravelListResponseDto(
                        2,
                        "강릉 갈사람",
                        "강릉",
                        "username",
                        "마감 D-228",
                        "오늘",
                        0,
                        2,
                        false,
                        true,
                        List.of("가성비", "핫플"),
                        "/api/travel/2",
                        "/api/travel/2/edit",
                        "/api/travel/2/delete",
                        "/api/bookmarks",
                        "/api/bookmarks/2"
                )
        );
        when(travelListService.getTravelListByUser(userNumber)).thenReturn(mockTravelList);

        // when & then
        mockMvc.perform(get("/api/my-travels")
                        .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].travelNumber").value(2))
                .andExpect(jsonPath("$[0].title").value("강릉 갈사람"))
                .andExpect(jsonPath("$[0].location").value("강릉"))
                .andExpect(jsonPath("$[0].username").value("username"))
                .andExpect(jsonPath("$[0].dday").value("마감 D-228"))
                .andExpect(jsonPath("$[0].postedAgo").value("오늘"))
                .andExpect(jsonPath("$[0].currentApplicants").value(0))
                .andExpect(jsonPath("$[0].maxPerson").value(2))
                .andExpect(jsonPath("$[0].completionStatus").value(false))
                .andExpect(jsonPath("$[0].bookmarked").value(true))
                .andExpect(jsonPath("$[0].tags[0]").value("가성비"))
                .andExpect(jsonPath("$[0].tags[1]").value("핫플"))
                .andExpect(jsonPath("$[0].detailUrl").value("/api/travel/2"))
                .andExpect(jsonPath("$[0].updateUrl").value("/api/travel/2/edit"))
                .andExpect(jsonPath("$[0].deleteUrl").value("/api/travel/2/delete"))
                .andExpect(jsonPath("$[0].addBookmarkUrl").value("/api/bookmarks"))
                .andExpect(jsonPath("$[0].removeBookmarkUrl").value("/api/bookmarks/2"));
    }
}
