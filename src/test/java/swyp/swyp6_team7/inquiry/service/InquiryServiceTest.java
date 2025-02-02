package swyp.swyp6_team7.inquiry.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import swyp.swyp6_team7.Inquiry.dto.InquiryRequestDto;
import swyp.swyp6_team7.Inquiry.InquiryType;
import swyp.swyp6_team7.Inquiry.service.InquiryService;
import swyp.swyp6_team7.Inquiry.util.InquiryEmailBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InquiryServiceTest {

    private InquiryService inquiryService;
    private JavaMailSender mailSender;
    private InquiryEmailBuilder inquiryEmailBuilder;

    @BeforeEach
    void setUp() {
        mailSender = Mockito.mock(JavaMailSender.class);
        inquiryService = new InquiryService(mailSender);
    }

    @Test
    @DisplayName("정상적으로 문의 이메일을 발송하는 경우")
    void sendInquiryEmail_Success() {
        // Given: 유효한 문의 데이터를 준비
        InquiryRequestDto inquiryRequestDto = new InquiryRequestDto(
                InquiryType.ACCOUNT_AND_LOGIN,
                "user@example.com",
                "로그인 오류",
                "로그인이 되지 않습니다. 확인 부탁드립니다."
        );

        // When: 이메일 발송 메서드를 호출
        inquiryService.sendInquiryEmail(inquiryRequestDto);

        // Then: JavaMailSender의 send 메서드가 호출되었는지 검증
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("발송 중 예외가 발생했을 때 RuntimeException을 발생시키는 경우")
    void sendInquiryEmail_Exception() {
        // Given: JavaMailSender가 예외를 발생시키도록 설정
        doThrow(new RuntimeException("메일 발송 실패")).when(mailSender).send(any(SimpleMailMessage.class));

        InquiryRequestDto inquiryRequestDto = new InquiryRequestDto(
                InquiryType.SERVICE_USAGE,
                "user@example.com",
                "서비스 이용 방법 문의",
                "서비스 사용법에 대해 알고 싶습니다."
        );

        // When & Then: 메서드 호출 시 RuntimeException 발생 여부 확인
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            inquiryService.sendInquiryEmail(inquiryRequestDto);
        });

        // JavaMailSender의 send 메서드가 호출되었는지 검증
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("문의 내용 이메일 본문이 정확히 구성되는 경우")
    void buildEmailContent_Test() {
        // Given: 유효한 문의 데이터를 준비
        InquiryRequestDto inquiryRequestDto = new InquiryRequestDto(
                InquiryType.INCONVENIENCE_OR_REPORT,
                "user@example.com",
                "불편 사항 신고",
                "서비스에서 발견한 불편 사항을 신고합니다."
        );

        // When: buildEmailContent 메서드를 직접 호출
        String emailContent = inquiryEmailBuilder.buildEmailContent(inquiryRequestDto);

        // Then: 이메일 본문의 내용이 예상대로 구성되는지 검증
        String expectedContent = String.format(
                "문의 유형: %s\n이메일: %s\n문의 제목: %s\n문의 내용: %s",
                "이용 불편 및 신고",
                "user@example.com",
                "불편 사항 신고",
                "서비스에서 발견한 불편 사항을 신고합니다."
        );

        org.junit.jupiter.api.Assertions.assertEquals(expectedContent, emailContent);
    }
}
