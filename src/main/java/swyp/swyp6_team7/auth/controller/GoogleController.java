package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.dto.SignupRequestDto;
import swyp.swyp6_team7.auth.dto.SocialLoginRedirectResponse;
import swyp.swyp6_team7.auth.dto.SocialUserSignUpResponse;
import swyp.swyp6_team7.auth.dto.UserInfoDto;
import swyp.swyp6_team7.auth.service.GoogleService;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.member.dto.UserCreateResponse;
import swyp.swyp6_team7.member.service.MemberService;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api")
public class GoogleController {

    private final GoogleService googleService;
    private final MemberService memberService;

    @Value("${google.client-id}")
    private String clientId;
    @Value("${google.redirect-uri}")
    private String redirectUri;

    public GoogleController(
            GoogleService googleService, MemberService memberService
    ) {
        this.googleService = googleService;
        this.memberService = memberService;
    }

    // 구글 로그인 리디렉션
    @GetMapping("/login/oauth/google")
    public ApiResponse<SocialLoginRedirectResponse> googleLoginRedirect(HttpSession session) {
        try {
            String state = UUID.randomUUID().toString();  // CSRF 방지용 state 값 생성
            session.setAttribute("oauth_state", state);   // 세션에 state 값 저장
            String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + clientId
                    + "&response_type=code"
                    + "&scope=email%20profile"  // 이메일과 프로필 정보 요청
                    + "&redirect_uri=" + redirectUri
                    + "&state=" + state;

            log.info("Google 로그인 리디렉션 성공: state={}", state);
            return ApiResponse.success(new SocialLoginRedirectResponse(googleAuthUrl));
        } catch (Exception e) {
            log.error("Google 로그인 리디렉션 중 오류 발생", e);
            throw new MoingApplicationException("Failed to create Google login redirect URL");
        }
    }

    // google 인증 후, 리다이렉트된 URI에서 코드를 처리
    @GetMapping("/login/oauth/google/callback")
    public ApiResponse<UserCreateResponse> googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpSession session) {

        // 세션에서 저장한 state 값 가져오기
        String sessionState = (String) session.getAttribute("oauth_state");
        if (sessionState != null && !sessionState.equals(state)) {
            log.warn("유효하지 않은 state 매개변수: sessionState={}, receivedState={}", sessionState, state);
            throw new MoingApplicationException("Invalid state parameter");
        }

        // Google 인증 코드로 로그인 처리
        try {
            UserInfoDto userInfo = googleService.processGoogleLogin(code);
            log.info("Google 로그인 처리 성공: userInfo={}", userInfo);
            return ApiResponse.success(memberService.getUserCreateResponse(userInfo));
        } catch (Exception e) {
            log.error("Google 로그인 처리 중 오류 발생", e);
            throw e;
        }
    }

    @PutMapping("/social/google/complete-signup")
    public ApiResponse<SocialUserSignUpResponse> completeGoogleSignup(@RequestBody SignupRequestDto signupData) {
        try {
            SocialUserSignUpResponse result = googleService.completeSignup(signupData);
            log.info("Google 회원가입 완료 정보 저장 성공: userNumber={}", result.getUserNumber());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Google 회원가입 완료 정보 저장 중 오류 발생", e);
            throw e;
        }
    }
}
