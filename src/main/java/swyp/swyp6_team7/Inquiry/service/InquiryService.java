package swyp.swyp6_team7.Inquiry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.Inquiry.dto.InquiryRequestDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class InquiryService {
    private final JavaMailSender mailSender;

    public void sendInquiryEmail(InquiryRequestDto inquiryRequestDto){
        log.info("문의 이메일 발송 시작 - 문의 제목:{}, 이메일: {}", inquiryRequestDto.getTitle(), inquiryRequestDto.getEmail());

        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("22mallow3@gmail.com");
            message.setSubject("[1:1 문의] "+inquiryRequestDto.getTitle());
            message.setText(buildEmailContent(inquiryRequestDto));

            mailSender.send(message);
            log.info("문의 이메일 발송 완료 - 문의 제목:{}, 이메일: {}", inquiryRequestDto.getTitle(), inquiryRequestDto.getEmail());
        } catch (Exception e){
            log.error("문의 이메일 발송 실패 - 문의 제목:{}, 이메일: {}", inquiryRequestDto.getTitle(), inquiryRequestDto.getEmail());
            throw new RuntimeException("문의 이메일 발송 중 오류가 발생했습니다.");
        }

    }
    private String buildEmailContent(InquiryRequestDto inquiryRequestDto){
        return String.format(
                "문의 유형: %s\n이메일: %s\n문의 제목: %s\n문의 내용: %s",
                inquiryRequestDto.getInquiryType().getDescription(),
                inquiryRequestDto.getEmail(),
                inquiryRequestDto.getTitle(),
                inquiryRequestDto.getContent()
        );
    }
}
