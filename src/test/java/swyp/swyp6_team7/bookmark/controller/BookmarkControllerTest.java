package swyp.swyp6_team7.bookmark.controller;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.bookmark.dto.BookmarkRequest;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.member.entity.Users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class BookmarkControllerTest extends IntegrationTest {

    private static String jwtToken;
    private static int userNumber;

    @BeforeAll
    public void setUp() {
        Users user = createUser("travel", "password");
        userNumber = user.getUserNumber();
        LoginTokenResponse tokenResponse = login("travel@test.com", "password");
        jwtToken = tokenResponse.getAccessToken();

        createTravel(user.getUserNumber(), "파리");
    }

    @Test
    @DisplayName("북마크 추가 테스트")
    @Order(1)
    public void testAddBookmark() throws Exception {
        BookmarkRequest request = new BookmarkRequest();
        request.setTravelNumber(1);

        mockMvc.perform(post("/api/bookmarks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 북마크 목록 조회 테스트")
    @Order(2)
    void testGetBookmarks() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content[0].travelNumber").value(1))
                .andExpect(jsonPath("$.success.content[0].title").value("여행 타이틀 " + userNumber))
                .andExpect(jsonPath("$.success.content[0].userName").value("travel"))
                .andExpect(jsonPath("$.success.page.size").value(5))
                .andExpect(jsonPath("$.success.page.number").value(0))
                .andExpect(jsonPath("$.success.page.totalElements").value(1))
                .andExpect(jsonPath("$.success.page.totalPages").value(1));
    }

    @Test
    @Order(3)
    @DisplayName("사용자의 북마크된 여행 번호 목록 조회 테스트")
    public void getBookmarkedTravelNumbers_ShouldReturnListOfTravelNumbers() throws Exception {
        // Given
        mockMvc.perform(get("/api/bookmarks/travel-number")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success[0]").value(1));
//                .andExpect(jsonPath("$.success[1]").value(102))
//                .andExpect(jsonPath("$.success[2]").value(103));
    }

    @Test
    @DisplayName("북마크 삭제 테스트")
    @Order(4)
    public void testRemoveBookmark() throws Exception {
        // Given
        int travelNumber = 1;

        mockMvc.perform(delete("/api/bookmarks/{travelNumber}", travelNumber)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }
}

