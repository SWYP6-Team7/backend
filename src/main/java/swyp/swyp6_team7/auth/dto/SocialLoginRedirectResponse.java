package swyp.swyp6_team7.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRedirectResponse {
    private String redirectUrl;

    public SocialLoginRedirectResponse(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
