package swyp.swyp6_team7.auth.controller;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.dto.LoginResponse;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.auth.service.LoginFacade;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.member.entity.Users;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LoginController {
    private final LoginFacade loginFacade;

    @PostMapping("/api/login")
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response
    ) {
        // 로그인 및 토큰 생성
        log.info("로그인 요청 - 이메일: {}", loginRequestDto.getEmail());

        LoginTokenResponse tokenResponse = loginFacade.login(loginRequestDto);

        Users user = tokenResponse.getUser();
        if (user.isBlocked()) {
            String token = loginFacade.getBlockToken(user);
            String redirectUrl = "https://www.alpha.moing.io/block?token=" + token;

            try {
                response.sendRedirect(redirectUrl);
            } catch (Exception e) {
                log.error("정지된 유저의 화면 이동에 실패했습니다.");
            }
            return ApiResponse.success(new LoginResponse(
                    user.getUserNumber(),
                    null
            ));
        }

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        String cookie = loginFacade.getCookie(refreshToken);
        response.addHeader("Set-Cookie", cookie);

        // Access Token과 userId를 포함하는 JSON 응답 반환
        LoginResponse loginResponse = new LoginResponse(
                user.getUserNumber(),
                accessToken
        );
        log.info("로그인 성공 - userNumber: {}", user.getUserNumber());
        return ApiResponse.success(loginResponse);
    }
}
