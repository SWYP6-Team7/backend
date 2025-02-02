package swyp.swyp6_team7.Inquiry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum InquiryType {
    ACCOUNT_AND_LOGIN("계정 및 로그인"),
    SERVICE_USAGE("서비스 이용 방법"),
    INCONVENIENCE_OR_REPORT("이용 불편 및 신고"),
    OTHER("기타 문의");

    private final String description;

    InquiryType(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static InquiryType fromDescription(String description) {
        return Arrays.stream(InquiryType.values())
                .filter(type -> type.description.equals(description))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid inquiry type: " + description));
    }
}
