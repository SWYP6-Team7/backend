package swyp.swyp6_team7.auth.controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

@RestController
@RequestMapping("/api/token")
@Slf4j
public class TokenController {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final JwtBlacklistService jwtBlacklistService;

    public TokenController(JwtProvider jwtProvider, UserRepository userRepository,
                           JwtBlacklistService jwtBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    // Refresh Token으로 새로운 Access Token 발급
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookies(request);
        log.info("Refresh Token으로 Access Token 재발급 요청");

        if (refreshToken == null) {
            log.warn("Refresh Token이 존재하지 않습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Refresh Token이 존재하지 않습니다."));
        }

        // Redis에서 Refresh Token이 블랙리스트에 있는지 확인
        if (jwtBlacklistService.isTokenBlacklisted(refreshToken)) {
            log.warn("블랙리스트에 등록된 Refresh Token 사용 시도: {}", refreshToken);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh Token이 블랙리스트에 있습니다. 다시 로그인 해주세요."));
        }

        try {
            if (jwtProvider.validateToken(refreshToken)) {
                // Refresh Token에서 이메일 정보 추출
                String userEmail = jwtProvider.getUserEmail(refreshToken);
                Users user = userRepository.findByUserEmail(userEmail)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다. 이메일: " + userEmail));
                log.info("사용자 확인 완료: email={}", userEmail);

                // 새로운 Access Token 발급
                String newAccessToken = jwtProvider.createAccessToken(user.getUserEmail(), user.getUserNumber(), List.of(user.getRole().name()));
                log.info("새로운 Access Token 발급 성공: userId={}, accessToken={}", user.getUserNumber(), newAccessToken);

                // 새로운 Access Token을 JSON 응답으로 반환
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("userId", String.valueOf(user.getUserNumber()));
                responseMap.put("accessToken", newAccessToken);
                return ResponseEntity.ok(responseMap);
            } else {
                log.warn("만료된 Refresh Token 사용 시도: {}", refreshToken);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh Token이 만료되었습니다. 다시 로그인 해주세요."));
            }
        } catch (JwtException e) {
            log.error("유효하지 않은 Refresh Token 사용 시도: {}", refreshToken, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Refresh Token이 유효하지 않습니다."));
        } catch (Exception e) {
            log.error("Access Token 재발급 중 알 수 없는 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Access Token 재발급에 실패했습니다."));
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
