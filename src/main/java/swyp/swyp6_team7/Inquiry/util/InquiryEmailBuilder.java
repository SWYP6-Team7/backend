package swyp.swyp6_team7.Inquiry.util;

import swyp.swyp6_team7.Inquiry.dto.InquiryRequestDto;

public class InquiryEmailBuilder {
    public static String buildEmailContent(InquiryRequestDto inquiryRequestDto) {
        return String.format(
                "문의 유형: %s\n이메일: %s\n문의 제목: %s\n문의 내용: %s",
                inquiryRequestDto.getInquiryType().getDescription(),
                inquiryRequestDto.getEmail(),
                inquiryRequestDto.getTitle(),
                inquiryRequestDto.getContent()
        );
    }
}