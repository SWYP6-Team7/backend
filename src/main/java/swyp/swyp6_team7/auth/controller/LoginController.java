package swyp.swyp6_team7.auth.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.service.LoginService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.HashMap;
import java.util.Map;

@RestController
//@RequestMapping("/api")
public class LoginController {
    private final LoginService loginService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final MemberService memberService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    public LoginController(LoginService loginService, UserLoginHistoryService userLoginHistoryService, MemberService memberService,UserRepository userRepository) {
        this.loginService = loginService;
        this.userLoginHistoryService = userLoginHistoryService;
        this.memberService = memberService;
        this.userRepository = userRepository;
    }


    @PostMapping("/api/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        if (loginRequestDto.getEmail() == null || loginRequestDto.getEmail().isEmpty() ||
                loginRequestDto.getPassword() == null || loginRequestDto.getPassword().isEmpty()) {
            logger.warn("이메일과 비밀번호가 입력되지 않았습니다.");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "이메일과 비밀번호는 필수입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // 400 Bad Request 반환
        }
        try {
            logger.info("로그인 요청 - 이메일: {}", loginRequestDto.getEmail());
            Map<String, String> tokenMap = loginService.login(loginRequestDto);
            String accessToken = tokenMap.get("accessToken");
            String refreshToken = tokenMap.get("refreshToken");

            // 리프레시 토큰을 HttpOnly 쿠키로 설정
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)  // HTTPS 환경에서만 전송
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)  // 쿠키의 만료 시간 설정 (예: 7일)
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            Users user = userRepository.findByUserEmail(loginRequestDto.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("사용자 이메일을 찾을 수 없습니다."));
            userLoginHistoryService.saveLoginHistory(user);  // 로그인 이력 저장
            memberService.updateLoginDate(user);  // 로그인 시간 업데이트


            // Access Token과 userId를 포함하는 JSON 응답 반환
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("userId", String.valueOf(user.getUserNumber()));
            responseMap.put("accessToken", accessToken);

            logger.info("로그인 성공 - userId: {}", user.getUserNumber());
            return ResponseEntity.ok(responseMap); // Access Token 반환

        } catch (UsernameNotFoundException e) {
            logger.error("로그인 실패 - Email Not Found: {}", loginRequestDto.getEmail(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);  // 404 Not Found 반환
        } catch (BadCredentialsException e) {
            logger.error("로그인 실패 - Unauthorized: {}", loginRequestDto.getEmail(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse); // 401 Unauthorized 반환
        } catch (IllegalArgumentException e) {
            logger.error("로그인 실패 - Bad Request", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);  // 400 Bad Request 반환
        }
    }


}
