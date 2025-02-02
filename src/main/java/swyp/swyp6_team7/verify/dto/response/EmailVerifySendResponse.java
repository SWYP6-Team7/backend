package swyp.swyp6_team7.verify.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerifySendResponse {

    private String sessionToken;
    private String email;

    public EmailVerifySendResponse(String sessionToken, String email) {
        this.sessionToken = sessionToken;
        this.email = email;
    }
}
