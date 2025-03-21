package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.dto.LoginResponse;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.auth.dto.SocialLoginRequestDto;
import swyp.swyp6_team7.auth.service.LoginFacade;
import swyp.swyp6_team7.global.utils.api.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social")
@Slf4j
public class SocialLoginController {

    private final LoginFacade loginFacade;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> handleSocialLogin(
            @RequestBody SocialLoginRequestDto loginRequest,
            HttpServletResponse response
    ) {
        String socialLoginId = loginRequest.getSocialLoginId();
        log.info("소셜 로그인 요청: socialLoginID={}", socialLoginId);

        LoginTokenResponse tokenResponse = loginFacade.socialLogin(socialLoginId);
        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        // 리프레시 토큰을 쿠키에 저장
        String cookie = loginFacade.getCookie(refreshToken);
        response.addHeader("Set-Cookie", cookie);

        // 액세스 토큰을 JSON 응답으로 반환
        LoginResponse loginResponse = new LoginResponse(
                tokenResponse.getUser().getUserNumber(),
                tokenResponse.getUser().getUserStatus(),
                accessToken,
                null
        );
        return ApiResponse.success(loginResponse);
    }
}
