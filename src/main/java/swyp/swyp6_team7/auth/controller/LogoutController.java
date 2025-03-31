package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.auth.service.TokenService;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.api.ErrorMessage;
import swyp.swyp6_team7.global.utils.api.ResultType;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;


@Slf4j
@RestController
@RequiredArgsConstructor
public class LogoutController {

    private final UserLoginHistoryService userLoginHistoryService;
    private final MemberService memberService;
    private final JwtBlacklistService jwtBlacklistService;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;


    @PostMapping("/api/logout")
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 현재 인증된 유저 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer userNumber = customUserDetails.getUserNumber();
            log.info("로그아웃 요청 - userNumber: {}", userNumber);

            // Access Token 처리
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String accessToken = authorizationHeader.substring(7);  // "Bearer " 이후 토큰 부분 추출

                if (jwtProvider.validateToken(accessToken)) {
                    // 블랙리스트에 토큰 추가
                    long expirationTime = jwtProvider.getExpiration(accessToken);
                    jwtBlacklistService.addToBlacklist(accessToken, expirationTime);
                }
            }

            // Redis에 저장된 Refresh Token 삭제
            tokenService.deleteRefreshToken(userNumber);

            // 클라이언트 측의 refreshToken 쿠키 삭제
            Cookie deleteCookie = new Cookie("refreshToken", null);
            deleteCookie.setMaxAge(0);
            deleteCookie.setPath("/");
            deleteCookie.setHttpOnly(true);
            response.addCookie(deleteCookie);

            //로그아웃 이력 및 마지막 접속 시간 업데이트
            Users user = memberService.findByUserNumber(userNumber);
            userLoginHistoryService.updateLogoutHistory(user);
            memberService.updateLogoutDate(user);

            // SecurityContext에서 인증 정보 제거
            SecurityContextHolder.clearContext();

            return ApiResponse.success("로그아웃 성공");
        } else {
            log.warn("로그아웃 요청 시 인증 정보가 없음");
            return ApiResponse.error(ResultType.ACCESS_DENIED, new ErrorMessage("Access Denied", "로그아웃 실패"));
        }

    }
}
