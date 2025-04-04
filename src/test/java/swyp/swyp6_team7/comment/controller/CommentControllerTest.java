package swyp.swyp6_team7.comment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.comment.domain.Comment;
import swyp.swyp6_team7.comment.dto.request.CommentCreateRequestDto;
import swyp.swyp6_team7.comment.dto.request.CommentUpdateRequestDto;
import swyp.swyp6_team7.comment.dto.response.CommentDetailResponseDto;
import swyp.swyp6_team7.comment.dto.response.CommentListReponseDto;
import swyp.swyp6_team7.comment.service.CommentService;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.global.utils.auth.MemberAuthorizeUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static swyp.swyp6_team7.comment.dto.response.CommentListReponseDto.formatDate;

class CommentControllerTest extends IntegrationTest {

    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String BEARER_TOKEN = "Bearer test-token";

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        // JWT 토큰 검증 로직 모킹
        when(jwtProvider.validateToken(Mockito.anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("댓글 생성 성공")
    @Order(1)
    @WithMockUser
    void createComment_Success() throws Exception {
        // Given
        Integer userNumber = 1;
        CommentCreateRequestDto request = new CommentCreateRequestDto("Test Comment", 0);
        CommentDetailResponseDto response = new CommentDetailResponseDto(
                1, userNumber, "Test Comment", 1, 0, LocalDateTime.now(), "community", 100);

        try (MockedStatic<MemberAuthorizeUtil> mockedStatic = mockStatic(MemberAuthorizeUtil.class)) {
            mockedStatic.when(MemberAuthorizeUtil::getLoginUserNumber).thenReturn(userNumber);

            // CommentService.create 메서드 모킹
            Comment createdComment = new Comment(1, userNumber, "Test Comment", 0,
                    LocalDateTime.now(), "community", 100);
            when(commentService.create(any(CommentCreateRequestDto.class), eq(userNumber), eq("community"), eq(100)))
                    .thenReturn(createdComment);

            // CommentService.getCommentByNumber 메서드 모킹
            when(commentService.getCommentByNumber(eq(1))).thenReturn(response);

            // When
            ResultActions result = mockMvc.perform(post("/api/community/100/comments")
                    .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success.commentNumber").value(1))
                    .andExpect(jsonPath("$.success.content").value("Test Comment"))
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    @Order(2)
    @WithMockUser
    void getComments_Success() throws Exception {
        // Given
        Integer userNumber = 1;
        LocalDateTime now = LocalDateTime.now();
        CommentListReponseDto commentResponse = new CommentListReponseDto(
                1,
                userNumber,
                "Test Comment",
                0,
                formatDate(now),
                "community",
                100,
                "Test Writer",
                10,
                5,
                false,
                100,
                "http://example.com/profile.jpg",
                false);

        List<CommentListReponseDto> commentList = Collections.singletonList(commentResponse);
        Page<CommentListReponseDto> commentPage = new PageImpl<>(commentList, PageRequest.of(0, 5), 1);

        try (MockedStatic<MemberAuthorizeUtil> mockedStatic = mockStatic(MemberAuthorizeUtil.class)) {
            mockedStatic.when(MemberAuthorizeUtil::getLoginUserNumber).thenReturn(userNumber);

            // CommentService.getListPage 메서드 모킹
            when(commentService.getListPage(any(PageRequest.class), eq("community"), eq(100), eq(userNumber)))
                    .thenReturn(commentPage);

            // When
            ResultActions result = mockMvc.perform(get("/api/community/100/comments")
                    .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                    .param("page", "0")
                    .param("size", "5")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success.content[0].commentNumber").value(1))
                    .andExpect(jsonPath("$.success.content[0].content").value("Test Comment"))
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("댓글 수정 성공")
    @Order(3)
    @WithMockUser
    void updateComment_Success() throws Exception {
        // Given
        Integer userNumber = 1;
        CommentUpdateRequestDto request = new CommentUpdateRequestDto("Updated Comment");
        CommentDetailResponseDto response = new CommentDetailResponseDto(
                1, userNumber, "Updated Comment", 1, 0, LocalDateTime.now(), "community", 100);

        try (MockedStatic<MemberAuthorizeUtil> mockedStatic = mockStatic(MemberAuthorizeUtil.class)) {
            mockedStatic.when(MemberAuthorizeUtil::getLoginUserNumber).thenReturn(userNumber);

            // CommentService.update 메서드 모킹
            when(commentService.update(any(CommentUpdateRequestDto.class), eq(userNumber), eq(1)))
                    .thenReturn(response);

            // When
            ResultActions result = mockMvc.perform(put("/api/comments/1")
                    .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.success.content").value("Updated Comment"))
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @Order(4)
    @WithMockUser
    void deleteComment_Success() throws Exception {
        // Given
        Integer userNumber = 1;

        try (MockedStatic<MemberAuthorizeUtil> mockedStatic = mockStatic(MemberAuthorizeUtil.class)) {
            mockedStatic.when(MemberAuthorizeUtil::getLoginUserNumber).thenReturn(userNumber);

            // CommentService.delete 메서드 모킹
            Mockito.doNothing().when(commentService).delete(eq(1), eq(userNumber));

            // When
            ResultActions result = mockMvc.perform(delete("/api/comments/1")
                    .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andExpect(status().isOk())
                    .andDo(print());
        }
    }
}