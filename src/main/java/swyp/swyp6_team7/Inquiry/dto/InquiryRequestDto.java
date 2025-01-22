package swyp.swyp6_team7.Inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.Inquiry.InquiryType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequestDto {
    private InquiryType inquiryType;
    @NotBlank
    private String email;
    @Size(max=20)
    private String title;
    @Size(max=2000)
    private String content;
}
