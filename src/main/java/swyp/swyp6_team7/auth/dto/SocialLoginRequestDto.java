package swyp.swyp6_team7.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialLoginRequestDto {
    @NotNull
    private String email;
    @NotNull
    private String socialLoginId;
}
