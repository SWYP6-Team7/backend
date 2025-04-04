package swyp.swyp6_team7.auth.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import swyp.swyp6_team7.member.entity.UserStatus;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    @NotNull
    private Integer userId;
    private UserStatus status;

    @Nullable
    private String accessToken;

    @Nullable
    private String redirectUrl;
}
