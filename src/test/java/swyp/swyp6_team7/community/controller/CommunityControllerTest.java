package swyp.swyp6_team7.community.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.community.dto.request.CommunityCreateRequestDto;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.service.CommunityService;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.mock.WithMockCustomUser;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CommunityControllerTest extends IntegrationTest {

    @MockBean
    private CommunityService communityService;

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

        when(communityService.create(any(CommunityCreateRequestDto.class), any(Integer.class)))
                .thenReturn(new CommunityDetailResponseDto(1, 100, "Test User", 2, "잡담", "Test Title", "Test Content",
                        LocalDateTime.now(), 5, 100, 10, true, "http://example.com/profile.jpg"));

        mockMvc.perform(post("/api/community/posts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(userNumber = 100)
    @DisplayName("게시글 상세 조회 성공")
    void getCommunityDetail_Success() throws Exception {
        mockMvc.perform(get("/api/community/posts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
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

        when(communityService.update(any(CommunityUpdateRequestDto.class), any(Integer.class), any(Integer.class)))
                .thenReturn(new CommunityDetailResponseDto(1, 100, "Test User", 2, "Updated Category", "Updated Title",
                        "Updated Content", LocalDateTime.now(), 5, 100, 10, true, "http://example.com/profile.jpg"));

        mockMvc.perform(put("/api/community/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
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
