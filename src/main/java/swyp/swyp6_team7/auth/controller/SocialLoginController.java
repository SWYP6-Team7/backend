package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.SocialLoginService;
import swyp.swyp6_team7.member.entity.SocialUsers;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.SocialUserRepository;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/social")
@Slf4j
public class SocialLoginController {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final SocialLoginService socialLoginService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final MemberService memberService;

    public SocialLoginController(JwtProvider jwtProvider,
                                 UserRepository userRepository,
                                 SocialLoginService socialLoginService,
                                 UserLoginHistoryService userLoginHistoryService,
                                 MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.socialLoginService = socialLoginService;
        this.userLoginHistoryService = userLoginHistoryService;
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> handleSocialLogin(@RequestBody Map<String, String> loginRequest,
                                                                 HttpServletResponse response) {
        String socialLoginId = loginRequest.get("socialLoginId");
        String email = loginRequest.get("email");
        log.info("소셜 로그인 요청: socialLoginID={}, email={}", socialLoginId, email);

        try {
            Users user = socialLoginService.handleSocialLogin(socialLoginId, email);

            // JWT 토큰 생성
            String accessToken = jwtProvider.createAccessToken(
                    user.getUserNumber(),
                    List.of(user.getRole().name()));
            String refreshToken = jwtProvider.createRefreshToken(user.getUserNumber());
            log.info("JWT 토큰 생성 완료: accessToken={}, refreshToken=****", accessToken);

            // 리프레시 토큰을 쿠키에 저장
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 1주일
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);
            log.info("리프레시 토큰이 쿠키에 저장되었습니다");

            // 로그인 이력 저장
            userLoginHistoryService.saveLoginHistory(user);
            memberService.updateLoginDate(user);  // 로그인 시간 업데이트
            log.info("로그인 이력 저장 및 로그인 시간 업데이트 완료: userNumber={}", user.getUserNumber());

            // 액세스 토큰을 JSON 응답으로 반환
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("accessToken", accessToken);
            responseMap.put("userId", String.valueOf(user.getUserNumber()));

            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생: socialLoginID={}, email={}", socialLoginId, email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "소셜 로그인 처리에 실패했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

}
