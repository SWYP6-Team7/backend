package swyp.swyp6_team7.auth.controller;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.auth.service.LoginService;
import swyp.swyp6_team7.auth.service.TokenService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final MemberService memberService;
    private final TokenService tokenService;
    private final JwtProvider jwtProvider;
    private final JwtBlacklistService jwtBlacklistService;


    @PostMapping("/api/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {


        // 로그인 및 토큰 생성
        log.info("로그인 요청 - 이메일: {}", loginRequestDto.getEmail());
        Map<String, String> tokenMap = loginService.login(loginRequestDto);
        String accessToken = tokenMap.get("accessToken");
        String refreshToken = tokenMap.get("refreshToken");

        // 로그인 성공 후 Redis에 RefreshToken 저장
        Integer userNumber = loginService.getUserNumberByEmail(loginRequestDto.getEmail());
        tokenService.storeRefreshToken(userNumber, refreshToken);

        // 리프레시 토큰을 HttpOnly 쿠키로 설정 (TTL 7일)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(tokenService.getRefreshTokenValidity())
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // 로그인 이력 및 마지막 로그인 시간 업데이트
        Users user = loginService.getUserByEmail(loginRequestDto.getEmail());
        userLoginHistoryService.saveLoginHistory(user);
        memberService.updateLoginDate(user);

        // Access Token과 userId를 포함하는 JSON 응답 반환
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("userId", String.valueOf(user.getUserNumber()));
        responseMap.put("accessToken", accessToken);

        log.info("로그인 성공 - userNumber: {}", user.getUserNumber());
        return ResponseEntity.ok(responseMap);
    }
}
