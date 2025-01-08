package swyp.swyp6_team7.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.request.CommentUpdateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.comment.service.CommentService;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.mock.WithMockCustomUser;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static swyp.swyp6_team7.comment.dto.response.CommentListReponseDto.formatDate;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("댓글 생성 성공")
    @WithMockCustomUser(userNumber = 1)
    void createComment_Success() throws Exception {
        // Given
        CommentCreateRequestDto request = new CommentCreateRequestDto("Test Comment", 0);
        Comment createdComment = new Comment(1, 1, "Test Comment", 1,
                LocalDateTime.now(), "community", 100);
        CommentDetailResponseDto response = new CommentDetailResponseDto(
                1,1, "Test Comment",1, 0, LocalDateTime.now(), "community", 100);

        given(commentService.create(any(CommentCreateRequestDto.class), anyInt(), eq("community"), eq(100)))
                .willReturn(createdComment);
        given(commentService.getCommentByNumber(1)).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(post("/api/community/100/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentNumber").value(1))
                .andExpect(jsonPath("$.content").value("Test Comment"))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    @WithMockCustomUser(userNumber = 1)
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
        given(commentService.getListPage(any(), eq("community"), eq(100), anyInt()))
                .willReturn(new PageImpl<>(Collections.singletonList(commentResponse)));

        // When
        ResultActions result = mockMvc.perform(get("/api/community/100/comments")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].commentNumber").value(1))
                .andExpect(jsonPath("$.content[0].content").value("Test Comment"))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    @WithMockCustomUser(userNumber = 1)
    void updateComment_Success() throws Exception {
        // Given
        Comment original = new Comment(1, 1, "Test Comment", 1,
                LocalDateTime.now().minusDays(1), "community", 100);
        CommentUpdateRequestDto request = new CommentUpdateRequestDto("Updated Comment");
        CommentDetailResponseDto response = new CommentDetailResponseDto(
                1,1, "Updated Comment",1, 0, LocalDateTime.now(), "community", 100);

        given(commentService.update(any(CommentUpdateRequestDto.class), anyInt(), eq(1)))
                .willReturn(response);

        // When
        ResultActions result = mockMvc.perform(put("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Comment"))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @WithMockCustomUser(userNumber = 1)
    void deleteComment_Success() throws Exception {
        // Given
        willDoNothing().given(commentService).delete(eq(1), anyInt());

        // When
        ResultActions result = mockMvc.perform(delete("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isNoContent())
                .andDo(print());
    }
}
