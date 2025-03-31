package swyp.swyp6_team7.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final JwtProvider jwtProvider;
    private final JwtBlacklistService jwtBlacklistService;
    private final TokenService tokenService;
    private final MemberService memberService;
    private final UserLoginHistoryService userLoginHistoryService;

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            Integer userNumber = customUserDetails.getUserNumber();

            // Access Token 블랙리스트 처리
            String token = extractAccessToken(request);
            if (token != null && jwtProvider.validateToken(token)) {
                long expirationTime = jwtProvider.getExpiration(token);
                jwtBlacklistService.addToBlacklist(token, expirationTime);
            }

            // Refresh Token 삭제
            tokenService.deleteRefreshToken(userNumber);

            // 쿠키 제거
            expireRefreshTokenCookie(response);

            // 세션 무효화 처리 추가
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // 로그아웃 이력 기록
            Users user = memberService.findByUserNumber(userNumber);
            userLoginHistoryService.updateLogoutHistory(user);
            memberService.updateLogoutDate(user);

            // 인증 정보 초기화
            SecurityContextHolder.clearContext();
        }
    }

    private String extractAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void expireRefreshTokenCookie(HttpServletResponse response) {
        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setMaxAge(0);
        deleteCookie.setPath("/");
        deleteCookie.setHttpOnly(true);
        response.addCookie(deleteCookie);
    }
}

