package swyp.swyp6_team7.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.community.dto.request.CommunityCreateRequestDto;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.service.CommunityService;
import swyp.swyp6_team7.community.service.CommunityListService;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.mock.WithMockCustomUser;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommunityService communityService;
    @MockBean
    private CommunityListService communityListService;
    @MockBean
    private CategoryRepository categoryRepository;
    @MockBean
    private JwtProvider jwtProvider;
    @Autowired
    private ObjectMapper objectMapper;


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


        given(communityService.create(any(CommunityCreateRequestDto.class), eq(100)))
                .willReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/api/community/posts")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // When & Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.postNumber").value(1))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.categoryName").value("잡담"))
                .andExpect(jsonPath("$.likeCount").value(10));
        then(communityService).should().create(any(CommunityCreateRequestDto.class), eq(100));

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
        given(communityService.increaseView(eq(1), any()))
                .willReturn(response);

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/community/posts/1")
                .principal(() -> "user1"));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.postNumber").value(1))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andDo(print());

        then(communityService).should().increaseView(eq(1), any());
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

        given(communityService.update(any(CommunityUpdateRequestDto.class), eq(1), eq(100)))
                .willReturn(response);

        // When
        ResultActions resultActions = mockMvc.perform(put("/api/community/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.postNumber").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"))
                .andExpect(jsonPath("$.categoryName").value("Updated Category"))
                .andDo(print());

        then(communityService).should().update(any(CommunityUpdateRequestDto.class), eq(1), eq(100));
    }

    @Test
    @WithMockCustomUser(userNumber = 100)
    @DisplayName("게시글 삭제 성공")
    void deleteCommunityPost_Success() throws Exception {
        // Given
        doNothing().when(communityService).delete(eq(1), eq(100));

        // When
        ResultActions resultActions = mockMvc.perform(delete("/api/community/posts/1"));

        // Then
        resultActions.andExpect(status().isNoContent())
                .andDo(print());

        then(communityService).should().delete(eq(1), eq(100));
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getCommunityList_Success() throws Exception {
        given(communityListService.getCommunityList(any(), any(), any()))
                .willReturn(Page.empty());

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/community/posts")
                .param("page", "0")
                .param("size", "10"));

        // Then
        resultActions.andExpect(status().isOk())
                .andDo(print());

        then(communityListService).should().getCommunityList(any(), any(), any());
    }
}
