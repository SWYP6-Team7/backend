package swyp.swyp6_team7.auth.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.service.CustomUserDetails;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class LogoutController {
    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    private final UserLoginHistoryService userLoginHistoryService;
    private final MemberService memberService;

    public LogoutController(UserLoginHistoryService userLoginHistoryService, MemberService memberService) {
        this.userLoginHistoryService = userLoginHistoryService;
        this.memberService = memberService;
    }

    @PostMapping("/api/logout")
    public ResponseEntity<String> logout() {
        // 현재 인증된 유저 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            logger.info("Authentication principal: {}", authentication.getPrincipal());
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                logger.info("Authenticated user's email: {}", customUserDetails.getUsername());

                // 이메일로 Users 엔티티를 찾음
                Users user = memberService.getUserByEmail(customUserDetails.getUsername());

                // 로그아웃 이력 업데이트
                userLoginHistoryService.updateLogoutHistory(user);
                memberService.updateLogoutDate(user);

                // SecurityContext에서 인증 정보 제거
                SecurityContextHolder.clearContext();

                return ResponseEntity.ok("Logout successful");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No user is logged in");

    }
}
