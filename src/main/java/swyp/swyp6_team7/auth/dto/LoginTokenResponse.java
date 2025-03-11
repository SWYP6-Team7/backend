package swyp.swyp6_team7.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginTokenResponse {
    @NotNull
    private Integer userNumber;
    private String accessToken;
    private String refreshToken;
}
