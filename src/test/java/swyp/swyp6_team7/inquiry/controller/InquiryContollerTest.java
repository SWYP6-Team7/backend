
package swyp.swyp6_team7.inquiry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import swyp.swyp6_team7.Inquiry.controller.InquiryController;
import swyp.swyp6_team7.Inquiry.dto.InquiryRequestDto;
import swyp.swyp6_team7.Inquiry.InquiryType;
import swyp.swyp6_team7.Inquiry.service.InquiryService;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InquiryService inquiryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1:1 문의 성공적으로 접수되는 경우")
    void submitInquiry_Success() throws Exception {
        // Given
        InquiryRequestDto inquiryRequestDto = new InquiryRequestDto(
                InquiryType.ACCOUNT_AND_LOGIN,
                "user@example.com",
                "로그인 문제",
                "로그인이 되지 않습니다."
        );


        doNothing().when(inquiryService).sendInquiryEmail(Mockito.any(InquiryRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/inquiry/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inquiryRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("SUCCESS"))
                .andExpect(jsonPath("$.success").value("문의 접수 완료"));
    }

    @Test
    @DisplayName("1:1 문의 접수 시 요청 데이터가 잘못된 경우")
    void submitInquiry_InvalidRequest() throws Exception {
        // Given
        InquiryRequestDto inquiryRequestDto = new InquiryRequestDto(
                InquiryType.ACCOUNT_AND_LOGIN,
                "", // 잘못된 이메일
                "로그인 문제",
                "로그인이 되지 않습니다."
        );

        // When & Then
        mockMvc.perform(post("/api/inquiry/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inquiryRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("1:1 문의 이메일 발송 중 예외가 발생한 경우")
    void submitInquiry_EmailException() throws Exception {
        // Given
        InquiryRequestDto inquiryRequestDto = new InquiryRequestDto(
                InquiryType.SERVICE_USAGE,
                "user@example.com",
                "서비스 문의",
                "서비스 사용법을 알고 싶습니다."
        );


        Mockito.doThrow(new RuntimeException("이메일 발송 실패"))
                .when(inquiryService).sendInquiryEmail(Mockito.any(InquiryRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/inquiry/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inquiryRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.resultType").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.reason").value("서버 에러가 발생했습니다."));
    }
}