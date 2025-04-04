package swyp.swyp6_team7.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import swyp.swyp6_team7.member.entity.Users;

@Getter
@AllArgsConstructor
public class LoginTokenResponse {
    @NotNull
    private Users user;
    private String accessToken;
    private String refreshToken;
}
