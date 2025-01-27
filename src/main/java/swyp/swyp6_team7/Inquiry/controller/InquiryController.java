package swyp.swyp6_team7.Inquiry.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.Inquiry.dto.InquiryRequestDto;
import swyp.swyp6_team7.Inquiry.service.InquiryService;
import swyp.swyp6_team7.global.utils.api.ApiResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/inquiry")
public class InquiryController {
    private final InquiryService inquiryService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitInquiry(@Valid @RequestBody InquiryRequestDto inquiryRequestDto) {
        log.info("1:1 문의 접수 요청");

        inquiryService.sendInquiryEmail(inquiryRequestDto);

        log.info("1:1 문의 접수 완료 - 문의 제목: {}, 이메일: {}", inquiryRequestDto.getTitle(), inquiryRequestDto.getEmail());

        return ResponseEntity.ok(ApiResponse.success("문의 접수 완료"));

    }
}
