package swyp.swyp6_team7.verify.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerifySendRequest {

    @NotNull
    private String email;

    public EmailVerifySendRequest(String email) {
        this.email = email;
    }
}
