package swyp.swyp6_team7.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.service.LoginService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

@RestController
//@RequestMapping("/api")
public class LoginController {
    private final LoginService loginService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final MemberService memberService;

    public LoginController(LoginService loginService, UserLoginHistoryService userLoginHistoryService, MemberService memberService) {
        this.loginService = loginService;
        this.userLoginHistoryService = userLoginHistoryService;
        this.memberService = memberService;
    }


    @PostMapping("/api/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        try {
            // LoginService에서 실제로 로그인 유저를 가져오도록 수정
            String accessToken = loginService.login(loginRequestDto, response);
            Users user = loginService.getUserByEmail(loginRequestDto.getEmail()); // 로그인한 유저 정보 가져오기
            userLoginHistoryService.saveLoginHistory(user);  // 로그인 이력 저장
            memberService.updateLoginDate(user);  // 로그인 시간 업데이트
            return ResponseEntity.ok("Bearer " + accessToken); // Access Token 반환
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());  // 404 Not Found 반환
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());  // 400 Bad Request 반환
        }
    }


}
