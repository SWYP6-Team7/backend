package swyp.swyp6_team7.bookmark.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.bookmark.dto.BookmarkRequest;
import swyp.swyp6_team7.bookmark.dto.BookmarkResponse;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.entity.Users;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookmarkControllerTest extends IntegrationTest {

    private static String jwtToken;

    @BeforeAll
    public void setUp() {
        Users user = createUser("test", "password");
        LoginTokenResponse tokenResponse = login("test@test.com", "password");
        jwtToken = tokenResponse.getAccessToken();
    }

    @Test
    @DisplayName("북마크 추가 테스트")
    @Transactional
    public void testAddBookmark() throws Exception {
//        setUp();
        BookmarkRequest request = new BookmarkRequest();
        request.setTravelNumber(1);

        mockMvc.perform(post("/api/bookmarks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("북마크 삭제 테스트")
    @Transactional
    public void testRemoveBookmark() throws Exception {
        // Given
//        setUp();
        int travelNumber = 1;

        mockMvc.perform(delete("/api/bookmarks/{travelNumber}", travelNumber)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 북마크 목록 조회 테스트")
    @Transactional
    void testGetBookmarks() throws Exception {
//        setUp();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        LocalDateTime createdAt = LocalDateTime.parse("2024-10-02 21:56", dateTimeFormatter);
        BookmarkResponse response = new BookmarkResponse(
                1,
                "제목",
                "제주",
                1,
                "작성자",
                List.of("가성비", "핫플"),
                1,
                4,
                createdAt,
                true);

        List<BookmarkResponse> responses = List.of(response);
        PageRequest pageable = PageRequest.of(0, 5);
        Page<BookmarkResponse> pageResponse = new PageImpl<>(responses, pageable, responses.size());

        mockMvc.perform(get("/api/bookmarks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content[0].travelNumber").value(1))
                .andExpect(jsonPath("$.success.content[0].title").value("제목"))
                .andExpect(jsonPath("$.success.content[0].userName").value("작성자"))
                .andExpect(jsonPath("$.success.page.size").value(5))
                .andExpect(jsonPath("$.success.page.number").value(0))
                .andExpect(jsonPath("$.success.page.totalElements").value(1))
                .andExpect(jsonPath("$.success.page.totalPages").value(1));
    }

    @Test
    @Transactional
    @DisplayName("사용자의 북마크된 여행 번호 목록 조회 테스트")
    public void getBookmarkedTravelNumbers_ShouldReturnListOfTravelNumbers() throws Exception {
        // Given
//        setUp();
        List<Integer> travelNumbers = List.of(101, 102, 103);
        mockMvc.perform(get("/api/bookmarks/travel-number")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success[0]").value(101))
                .andExpect(jsonPath("$.success[1]").value(102))
                .andExpect(jsonPath("$.success[2]").value(103));
    }
}

