package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.dto.SocialLoginRedirectResponse;
import swyp.swyp6_team7.auth.dto.UserInfoDto;
import swyp.swyp6_team7.auth.service.NaverService;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class NaverController {

    private final NaverService naverService;
    @Value("${naver.client-id}")
    private String clientId;
    @Value("${naver.redirect-uri}")
    private String redirectUri;

    public NaverController(NaverService naverService) {
        this.naverService = naverService;
    }

    // 네이버 로그인 리다이렉트 URL
    @GetMapping("/login/oauth/naver")
    public ApiResponse<SocialLoginRedirectResponse> naverLoginRedirect(HttpServletResponse response, HttpSession session) throws IOException {
        try {
            String state = UUID.randomUUID().toString();  // CSRF 방지용 state 값 생성
            session.setAttribute("oauth_state", state);   // 세션에 state 값 저장

            String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize?client_id=" + clientId
                    + "&response_type=code"
                    + "&redirect_uri=" + redirectUri
                    + "&state=" + state;

            log.info("Naver 로그인 리디렉션 성공: state={}", state);
            return ApiResponse.success(new SocialLoginRedirectResponse(naverAuthUrl));
        } catch (Exception e) {
            log.error("Naver 로그인 리디렉션 중 오류 발생", e);
            throw e;
        }
    }

    // 네이버 콜백 처리
    @GetMapping("/login/oauth/naver/callback")
    public ApiResponse<UserInfoDto> naverCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpSession session
    ) {

        // 세션에서 저장한 state 값 가져와서 비교
        String sessionState = (String) session.getAttribute("oauth_state");
        if (sessionState == null || !sessionState.equals(state)) {
            log.warn("유효하지 않은 state 매개변수: sessionState={}, receivedState={}", sessionState, state);
            throw new MoingApplicationException("Invalid state parameter");
        }
        // 중복된 code 값 사용 방지
        if (session.getAttribute("oauth_code") != null) {
            log.warn("중복된 인증 코드 사용 시도: code={}", code);
            throw new MoingApplicationException("Authorization code already used");
        }
        session.setAttribute("oauth_code", code);  // code 값 저장하여 중복 사용 방지

        try {
            UserInfoDto userInfo = naverService.processNaverLogin(code, state);
            log.info("Naver 로그인 처리 성공: userInfo={}", userInfo);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            log.error("Naver 로그인 처리 중 오류 발생", e);
            throw e;
        }
    }

}
