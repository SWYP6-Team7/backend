package swyp.swyp6_team7.comment.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.request.CommentUpdateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.global.IntegrationTest;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static swyp.swyp6_team7.comment.dto.response.CommentListReponseDto.formatDate;

class CommentControllerTest extends IntegrationTest {

    @Test
    @DisplayName("댓글 생성 성공")
    @Order(1)
    void createComment_Success() throws Exception {
        // Given
        CommentCreateRequestDto request = new CommentCreateRequestDto("Test Comment", 0);
        Comment createdComment = new Comment(1, 1, "Test Comment", 1,
                LocalDateTime.now(), "community", 100);
        CommentDetailResponseDto response = new CommentDetailResponseDto(
                1, 1, "Test Comment", 1, 0, LocalDateTime.now(), "community", 100);

        // When
        ResultActions result = mockMvc.perform(post("/api/community/100/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.commentNumber").value(1))
                .andExpect(jsonPath("$.success.content").value("Test Comment"))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    @Order(2)
    void getComments_Success() throws Exception {
        // Given
        CommentListReponseDto commentResponse = new CommentListReponseDto(
                1,
                1,
                "Test Comment",
                0,
                formatDate(LocalDateTime.now()),
                "community",
                100,
                "Test Writer",
                10,
                5,
                false,
                100,
                "http://example.com/profile.jpg",
                false);

        // When
        ResultActions result = mockMvc.perform(get("/api/community/100/comments")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content[0].commentNumber").value(1))
                .andExpect(jsonPath("$.success.content[0].content").value("Test Comment"))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    @Order(3)
    void updateComment_Success() throws Exception {
        // Given
        Comment original = new Comment(1, 1, "Test Comment", 1,
                LocalDateTime.now().minusDays(1), "community", 100);
        CommentUpdateRequestDto request = new CommentUpdateRequestDto("Updated Comment");
        CommentDetailResponseDto response = new CommentDetailResponseDto(
                1, 1, "Updated Comment", 1, 0, LocalDateTime.now(), "community", 100);

        // When
        ResultActions result = mockMvc.perform(put("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.content").value("Updated Comment"))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @Order(4)
    void deleteComment_Success() throws Exception {
        // Given

        // When
        ResultActions result = mockMvc.perform(delete("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andDo(print());
    }
}
