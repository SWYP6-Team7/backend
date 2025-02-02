package swyp.swyp6_team7.verify.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerifyCheckRequest {

    private String verifyCode;
    private String sessionToken;

    public EmailVerifyCheckRequest(String verifyCode, String sessionToken) {
        this.verifyCode = verifyCode;
        this.sessionToken = sessionToken;
    }
}
