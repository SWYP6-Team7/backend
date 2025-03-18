package swyp.swyp6_team7.community.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.community.dto.request.CommunityCreateRequestDto;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.mock.WithMockCustomUser;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CommunityControllerTest extends IntegrationTest {

    @Test
    @WithMockCustomUser(userNumber = 100)
    @DisplayName("게시글 생성 성공")
    void createCommunityPost_Success() throws Exception {
        // Given
        CommunityCreateRequestDto request = CommunityCreateRequestDto.builder()
                .title("Test Title")
                .content("Test Content")
                .categoryName("잡담")
                .build();
        CommunityDetailResponseDto response = CommunityDetailResponseDto.builder()
                .postNumber(1)
                .userNumber(100)
                .postWriter("Test User")
                .categoryNumber(2)
                .categoryName("잡담")
                .title("Test Title")
                .content("Test Content")
                .regDate(LocalDateTime.now())
                .commentCount(5)
                .viewCount(100)
                .likeCount(10)
                .liked(true)
                .profileImageUrl("http://example.com/profile.jpg")
                .build();

        ResultActions resultActions = mockMvc.perform(post("/api/community/posts")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // When & Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.postNumber").value(1))
                .andExpect(jsonPath("$.success.title").value("Test Title"))
                .andExpect(jsonPath("$.success.content").value("Test Content"))
                .andExpect(jsonPath("$.success.categoryName").value("잡담"))
                .andExpect(jsonPath("$.success.likeCount").value(10));
    }

    @Test
    @WithMockCustomUser(userNumber = 100)
    @DisplayName("게시글 상세 조회 성공")
    void getCommunityDetail_Success() throws Exception {
        // Given
        CommunityDetailResponseDto response = CommunityDetailResponseDto.builder()
                .postNumber(1)
                .userNumber(100)
                .postWriter("Test User")
                .categoryNumber(1)
                .categoryName("Test Category")
                .title("Test Title")
                .content("Test Content")
                .regDate(LocalDateTime.now())
                .commentCount(5)
                .viewCount(100)
                .likeCount(10)
                .liked(true)
                .profileImageUrl("http://example.com/profile.jpg")
                .build();

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/community/posts/1")
                .principal(() -> "user1"));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.postNumber").value(1))
                .andExpect(jsonPath("$.success.title").value("Test Title"))
                .andExpect(jsonPath("$.success.content").value("Test Content"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    @WithMockCustomUser(userNumber = 100)
    void updateCommunityPost_Success() throws Exception {
        // Given
        CommunityUpdateRequestDto request = new CommunityUpdateRequestDto(
                "Updated Category", // categoryName
                "Updated Title",    // title
                "Updated Content"   // content
        );
        CommunityDetailResponseDto response = CommunityDetailResponseDto.builder()
                .postNumber(1)
                .userNumber(100)
                .postWriter("Test User")
                .categoryNumber(1)
                .categoryName("Updated Category")
                .title("Updated Title")
                .content("Updated Content")
                .regDate(LocalDateTime.now())
                .commentCount(5)
                .viewCount(100)
                .likeCount(10)
                .liked(true)
                .profileImageUrl("http://example.com/profile.jpg")
                .build();

        // When
        ResultActions resultActions = mockMvc.perform(put("/api/community/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.postNumber").value(1))
                .andExpect(jsonPath("$.success.title").value("Updated Title"))
                .andExpect(jsonPath("$.success.content").value("Updated Content"))
                .andExpect(jsonPath("$.success.categoryName").value("Updated Category"))
                .andDo(print());
    }

    @Test
    @WithMockCustomUser(userNumber = 100)
    @DisplayName("게시글 삭제 성공")
    void deleteCommunityPost_Success() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(delete("/api/community/posts/1"));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getCommunityList_Success() throws Exception {

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/community/posts")
                .param("page", "0")
                .param("size", "10"));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print());
    }
}
