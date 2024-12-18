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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelListService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String BEARER_TOKEN = "Bearer test-token";

    @DisplayName("내가 만든 여행 게시글 목록 조회 테스트")
    @WithMockUser
    @Test
    void getMyCreatedTravels_ShouldReturnListOfCreatedTravels() throws Exception {
        // given
        String token = "Bearer test-token";
        Integer userNumber = 1;
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
        mockStatic(MemberAuthorizeUtil.class);
        when(MemberAuthorizeUtil.getLoginUserNumber()).thenReturn(userNumber);

        when(travelListService.getTravelListByUser(userNumber, pageable)).thenReturn(page);

        // then
        mockMvc.perform(get("/api/my-travels")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].travelNumber").value(25))
                .andExpect(jsonPath("$.content[0].title").value("호주 여행 같이 갈 사람 구해요"))
                .andExpect(jsonPath("$.content[0].userNumber").value(3))
                .andExpect(jsonPath("$.content[0].userName").value("김모잉"))
                .andExpect(jsonPath("$.content[0].tags[0]").value("즉흥"))
                .andExpect(jsonPath("$.content[0].nowPerson").value(1))
                .andExpect(jsonPath("$.content[0].maxPerson").value(5))
                .andExpect(jsonPath("$.content[0].createdAt").value("2024-10-02 21:56"))
                .andExpect(jsonPath("$.content[0].registerDue").value("2025-05-15"))
                //.andExpect(jsonPath("$.content[0].isBookmarked").value(true))
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }
}
