package swyp.swyp6_team7.auth.controller;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.service.CustomUserDetails;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class LogoutController {
    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);
    @Autowired
    private final UserLoginHistoryService userLoginHistoryService;
    @Autowired
    private final MemberService memberService;
    @Autowired
    private final JwtBlacklistService jwtBlacklistService;
    @Autowired
    private final JwtProvider jwtProvider;

    public LogoutController(UserLoginHistoryService userLoginHistoryService, MemberService memberService,
                            JwtBlacklistService jwtBlacklistService,JwtProvider jwtProvider) {
        this.userLoginHistoryService = userLoginHistoryService;
        this.memberService = memberService;
        this.jwtBlacklistService = jwtBlacklistService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
            logger.info("로그아웃 요청 수신");


        // 현재 인증된 유저 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            logger.info("현재 인증된 사용자: {}", authentication.getPrincipal());
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                logger.info("로그아웃 요청 - 이메일: {}", customUserDetails.getUsername());

                // 이메일로 Users 엔티티를 찾음
                Users user = memberService.getUserByEmail(customUserDetails.getUsername());

                // 로그아웃 이력 업데이트
                logger.debug("사용자 로그아웃 이력 업데이트 시작 - userId: {}", user.getUserNumber());
                userLoginHistoryService.updateLogoutHistory(user);
                memberService.updateLogoutDate(user);
                logger.info("로그아웃 이력 및 마지막 접속 시간 업데이트 완료 - userId: {}", user.getUserNumber());

                // Access Token 추출 (Authorization 헤더에서 추출)
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    String accessToken = authorizationHeader.substring(7);  // "Bearer " 이후 토큰 부분 추출

                    // Access Token이 유효한지 검증
                    if (jwtProvider.validateToken(accessToken)) {
                        // 토큰의 만료 시간을 추출
                        long expirationTime = jwtProvider.getExpiration(accessToken);

                        // 블랙리스트에 토큰 추가
                        jwtBlacklistService.addToBlacklist(accessToken, expirationTime);

                        logger.info("Access Token added to blacklist: {}", accessToken);
                    } else {
                        logger.warn("Invalid or expired Access Token");
                    }
                } else {
                    logger.warn("Authorization header is missing or invalid");
                }

                // 클라이언트 측의 refreshToken 쿠키 삭제
                Cookie deleteCookie = new Cookie("refreshToken", null);
                deleteCookie.setMaxAge(0);
                deleteCookie.setPath("/");
                deleteCookie.setHttpOnly(true);
                response.addCookie(deleteCookie);
                logger.info("Refresh Token 쿠키 삭제 완료");

                // SecurityContext에서 인증 정보 제거
                SecurityContextHolder.clearContext();
                logger.info("SecurityContext 인증 정보 제거 완료");


                return ResponseEntity.ok("Logout successful");
            } else {
                logger.warn("인증된 사용자가 CustomUserDetails가 아님");
            }
        } else {
            logger.warn("로그아웃 요청 시 인증 정보가 없음");
        }

        logger.error("로그아웃 실패 - 인증된 사용자가 없음");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No user is logged in");

    }
}
