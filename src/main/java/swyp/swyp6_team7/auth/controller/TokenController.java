package swyp.swyp6_team7.auth.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.dto.LoginResponse;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.auth.service.TokenService;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.exception.MoingAuthenticationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/token")
@Slf4j
@RequiredArgsConstructor
public class TokenController {

    private final JwtProvider jwtProvider;
    private final JwtBlacklistService jwtBlacklistService;
    private final TokenService tokenService;


    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookies(request);
        log.info("Refresh Token으로 Access Token 재발급 요청");

        if (refreshToken == null) {
            log.warn("Refresh Token이 존재하지 않습니다.");
            throw new MoingApplicationException("Refresh Token이 존재하지 않습니다.");
        }

        // Redis에서 Refresh Token이 블랙리스트에 있는지 확인
        if (jwtBlacklistService.isTokenBlacklisted(refreshToken)) {
            log.warn("블랙리스트에 등록된 Refresh Token 사용 시도: {}", refreshToken);
            // TODO: 403 Http Status 반환하도록 수정
            throw new MoingAuthenticationException("Refresh Token이 블랙리스트에 있습니다. 다시 로그인 해주세요.");
        }

        try {
            // Refresh Token으로 새로운 Access Token과 Refresh Token(회전) 발급
            Map<String, String> newTokens = tokenService.refreshWithLock(refreshToken);
            String newAccessToken = newTokens.get("accessToken");
            String newRefreshToken = newTokens.get("refreshToken");

            // 새로운 Refresh Token을 HttpOnly 쿠키로 갱신
            ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(tokenService.getRefreshTokenValidity())
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            LoginResponse loginResponse = new LoginResponse(
                    jwtProvider.getUserNumber(newAccessToken),
                    newAccessToken
            );
            return ApiResponse.success(loginResponse);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 Refresh Token 사용 시도: {}", refreshToken);
            throw new MoingApplicationException("Refresh Token이 만료되었습니다. 다시 로그인 해주세요.");
        } catch (JwtException e) {
            log.error("유효하지 않은 Refresh Token 사용 시도: {}", refreshToken, e);
            throw new MoingApplicationException("Refresh Token이 유효하지 않습니다.");
        } catch (Exception e) {
            log.error("Access Token 재발급 중 알 수 없는 오류 발생", e);
            throw e;
        }
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
