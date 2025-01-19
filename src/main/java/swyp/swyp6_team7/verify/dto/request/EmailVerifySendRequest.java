package swyp.swyp6_team7.verify.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerifySendRequest {

    private String email;

    public EmailVerifySendRequest(String email) {
        this.email = email;
    }
}
