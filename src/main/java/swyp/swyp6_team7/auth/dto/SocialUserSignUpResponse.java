package swyp.swyp6_team7.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SocialUserSignUpResponse {
    private Integer userNumber;
    private String message;
    private String socialLoginId;
    private String email;
}
