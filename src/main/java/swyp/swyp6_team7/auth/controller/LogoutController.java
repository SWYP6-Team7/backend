package swyp.swyp6_team7.auth.controller;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;


@Slf4j
@RestController
public class LogoutController {
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
        log.info("로그아웃 요청 수신");

        // 현재 인증된 유저 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("현재 인증된 사용자: {}", authentication.getPrincipal());
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                log.info("로그아웃 요청 - 사용자 번호: {}", customUserDetails.getUserNumber());

                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    String accessToken = authorizationHeader.substring(7);  // "Bearer " 이후 토큰 부분 추출

                    // Access Token이 유효한지 검증
                    if (jwtProvider.validateToken(accessToken)) {
                        // 토큰에서 userNumber 추출
                        Integer userNumber = jwtProvider.getUserNumber(accessToken);

                        // userNumber로 Users 엔티티를 찾음
                        Users user = memberService.findByUserNumber(userNumber);

                        // 로그아웃 이력 업데이트
                        log.debug("사용자 로그아웃 이력 업데이트 시작 - userId: {}", user.getUserNumber());
                        userLoginHistoryService.updateLogoutHistory(user);
                        memberService.updateLogoutDate(user);
                        log.info("로그아웃 이력 및 마지막 접속 시간 업데이트 완료 - userId: {}", user.getUserNumber());

                        // 블랙리스트에 토큰 추가
                        long expirationTime = jwtProvider.getExpiration(accessToken);
                        jwtBlacklistService.addToBlacklist(accessToken, expirationTime);

                        log.info("Access Token이 블랙리스트에 추가되었습니다.: {}", accessToken);
                    } else {
                        log.warn("유효하지 않거나 만료된 Access Token입니다");
                    }
                } else {
                    log.warn("Authorization 헤더가 없거나 유효하지 않습니다");
                }

                // 클라이언트 측의 refreshToken 쿠키 삭제
                Cookie deleteCookie = new Cookie("refreshToken", null);
                deleteCookie.setMaxAge(0);
                deleteCookie.setPath("/");
                deleteCookie.setHttpOnly(true);
                response.addCookie(deleteCookie);
                log.info("Refresh Token 쿠키 삭제 완료");

                // SecurityContext에서 인증 정보 제거
                SecurityContextHolder.clearContext();
                log.info("SecurityContext 인증 정보 제거 완료");

                return ResponseEntity.ok("로그아웃 성공");
            } else {
                log.warn("인증된 사용자가 CustomUserDetails가 아님");
            }
        } else {
            log.warn("로그아웃 요청 시 인증 정보가 없음");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        log.error("로그아웃 실패 - 인증된 사용자가 없음");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");

    }
}
