package swyp.swyp6_team7.notice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.notice.dto.NoticeRequestDto;
import swyp.swyp6_team7.notice.dto.NoticeResponseDto;
import swyp.swyp6_team7.notice.service.NoticeService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "kakao.client-id=fake-client-id",
        "kakao.client-secret=fake-client-secret",
        "kakao.redirect-uri=http://localhost:8080/login/oauth2/code/kakao",
        "kakao.token-url=https://kauth.kakao.com/oauth/token",
        "kakao.user-info-url=https://kapi.kakao.com/v2/user/me"
})
public class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeService noticeService;

    @Test
    @DisplayName("모든 공지 조회 테스트")
    public void testGetAllNotices() throws Exception {
        // Given
        NoticeResponseDto notice1 = new NoticeResponseDto(1L, "Notice 1", "Content 1", LocalDateTime.now(), 0);
        NoticeResponseDto notice2 = new NoticeResponseDto(2L, "Notice 2", "Content 2", LocalDateTime.now(), 0);
        List<NoticeResponseDto> notices = Arrays.asList(notice1, notice2);

        // When
        when(noticeService.getAllNotices()).thenReturn(notices);

        // Then
        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.length()").value(2))
                .andExpect(jsonPath("$.success[0].title").value("Notice 1"))
                .andExpect(jsonPath("$.success[1].title").value("Notice 2"));
    }

    @Test
    @DisplayName("ID로 공지 조회 테스트")
    public void testGetNoticeById() throws Exception {
        // Given
        NoticeResponseDto notice = new NoticeResponseDto(1L, "Notice 1", "Content 1", LocalDateTime.now(), 0);

        // When
        when(noticeService.getNoticeById(1L)).thenReturn(notice);

        // Then
        mockMvc.perform(get("/api/notices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.title").value("Notice 1"))
                .andExpect(jsonPath("$.success.content").value("Content 1"));
    }

    @Test
    @DisplayName("공지 생성 테스트")
    @WithMockUser(authorities = "ADMIN") // 권한 설정
    public void testCreateNotice() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "password", List.of(new SimpleGrantedAuthority("ADMIN")))
        );
        // Given
        NoticeRequestDto noticeRequest = new NoticeRequestDto("New Notice", "New Content");
        NoticeResponseDto noticeResponse = new NoticeResponseDto(1L, "New Notice", "New Content", LocalDateTime.now(), 0);

        // When
        when(noticeService.createNotice(any(NoticeRequestDto.class))).thenReturn(noticeResponse);

        // Then
        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Notice\", \"content\": \"New Content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.title").value("New Notice"))
                .andExpect(jsonPath("$.success.content").value("New Content"));
    }

    @Test
    @DisplayName("공지 수정 테스트")
    @WithMockUser(authorities = "ADMIN") // 권한 설정
    public void testUpdateNotice() throws Exception {
        // Given
        NoticeRequestDto noticeRequest = new NoticeRequestDto("Updated Notice", "Updated Content");
        NoticeResponseDto noticeResponse = new NoticeResponseDto(1L, "Updated Notice", "Updated Content", LocalDateTime.now(), 0);

        // When
        when(noticeService.updateNotice(anyLong(), any(NoticeRequestDto.class))).thenReturn(noticeResponse);

        // Then
        mockMvc.perform(put("/api/notices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Updated Notice\", \"content\": \"Updated Content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success.title").value("Updated Notice"))
                .andExpect(jsonPath("$.success.content").value("Updated Content"));
    }

    @Test
    @DisplayName("공지 삭제 테스트")
    @WithMockUser(authorities = "ADMIN") // 권한 설정
    public void testDeleteNotice() throws Exception {
        // Given
        doNothing().when(noticeService).deleteNotice(1L);

        // Then
        mockMvc.perform(delete("/api/notices/1"))
                .andExpect(status().isOk());
    }
}