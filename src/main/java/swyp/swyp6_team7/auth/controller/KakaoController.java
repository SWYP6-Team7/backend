package swyp.swyp6_team7.auth.controller;


import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.dto.SignupRequestDto;
import swyp.swyp6_team7.auth.dto.SocialLoginRedirectResponse;
import swyp.swyp6_team7.auth.dto.SocialUserSignUpResponse;
import swyp.swyp6_team7.auth.dto.UserInfoDto;
import swyp.swyp6_team7.auth.service.KakaoService;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api")
public class KakaoController {

    private final KakaoService kakaoService;
    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoController(KakaoService kakaoService) {
        this.kakaoService = kakaoService;
    }

    // 카카오 로그인 리디렉션
    @GetMapping("/login/oauth/kakao")
    public ApiResponse<SocialLoginRedirectResponse> kakaoLoginRedirect(HttpSession session) {
        try {
            String state = UUID.randomUUID().toString();  // CSRF 방지용 state 값 생성
            session.setAttribute("oauth_state", state);   // 세션에 state 값 저장

            String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId
                    + "&response_type=code"
                    + "&redirect_uri=" + redirectUri
                    + "&state=" + state;

            log.info("Kakao 로그인 리디렉션 성공: state={}", state);
            return ApiResponse.success(new SocialLoginRedirectResponse(kakaoAuthUrl));
        } catch (Exception e) {
            log.error("Kakao 로그인 리디렉션 중 오류 발생", e);
            throw e;
        }
    }

    // 카카오 인증 후, 리다이렉트된 URI에서 코드를 처리
    @GetMapping("/login/oauth/kakao/callback")
    public ApiResponse<UserInfoDto> kakaoCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpSession session) {

        // 세션에서 저장한 state 값 가져오기 (선택 사항)
        String sessionState = (String) session.getAttribute("oauth_state");
        if (sessionState != null && !sessionState.equals(state)) {
            log.warn("유효하지 않은 state 매개변수: sessionState={}, receivedState={}", sessionState, state);
            throw new MoingApplicationException("Invalid state parameter");
        }

        // 카카오 인증 코드로 로그인 처리
        try {
            UserInfoDto userInfo = kakaoService.processKakaoLogin(code);
            log.info("Kakao 로그인 처리 성공: userInfo={}", userInfo);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            log.error("Kakao 로그인 처리 중 오류 발생", e);
            throw e;
        }
    }

    @PutMapping("/social/kakao/complete-signup")
    public ApiResponse<SocialUserSignUpResponse> completeKakaoSignup(@RequestBody SignupRequestDto signupData) {
        try {
            SocialUserSignUpResponse result = kakaoService.completeSignup(signupData);
            log.info("Kakao 회원가입 완료 정보 저장 성공: userNumber={}", result.getUserNumber());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Kakao 회원가입 완료 정보 저장 중 오류 발생", e);
            throw e;
        }
    }
}
