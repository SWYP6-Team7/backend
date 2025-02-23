package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.dto.LoginResponse;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.SocialLoginService;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.member.entity.SocialUsers;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social")
@Slf4j
public class SocialLoginController {

    private final JwtProvider jwtProvider;
    private final SocialLoginService socialLoginService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final MemberService memberService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> handleSocialLogin(@RequestBody Map<String, String> loginRequest,
                                                        HttpServletResponse response) {
        String socialLoginId = loginRequest.get("socialLoginId");
        String email = loginRequest.get("email");
        log.info("소셜 로그인 요청: socialLoginID={}, email={}", socialLoginId, email);

        try {
            // 소셜 사용자 정보 확인
            Optional<SocialUsers> socialUserOpt = socialLoginService.findSocialUserByLoginId(socialLoginId);

            if (socialUserOpt.isPresent()) {
                SocialUsers socialUser = socialUserOpt.get();

                // 삭제된 유저인지 확인
                Users user = socialUser.getUser();
                if (user.getUserStatus() == UserStatus.DELETED) {
                    log.warn("소셜 로그인 실패 - 삭제된 계정: userNumber={}, email={}", user.getUserNumber(), email);
                    throw new MoingApplicationException("해당 계정은 탈퇴된 상태입니다. 자세한 사항은 관리자에 문의하세요.");
                }
            }

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
            LoginResponse loginResponse = new LoginResponse(
                    user.getUserNumber(),
                    accessToken
            );
            return ApiResponse.success(loginResponse);
        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생: socialLoginID={}, email={}", socialLoginId, email, e);
            throw e;
        }
    }

}
