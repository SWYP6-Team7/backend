package swyp.swyp6_team7.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import swyp.swyp6_team7.auth.service.CustomUserDetails;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final JwtBlacklistService jwtBlacklistService;

    public JwtFilter(JwtProvider jwtProvider, UserDetailsService userDetailsService,
                     UserLoginHistoryService userLoginHistoryService,JwtBlacklistService jwtBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.userLoginHistoryService = userLoginHistoryService;
        this.jwtBlacklistService = jwtBlacklistService;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return  path.startsWith("/login/oauth/google")||
                path.startsWith("/login/oauth/naver") ||
                path.startsWith("/login/oauth/kakao") ||
                path.equals("/api/login") ||
                path.equals("/api/users/new") ||
                path.equals("/api/refresh-token"); // 로그인 및 회원가입 경로 필터링 제외
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더에서 JWT 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        String userEmail = null;


        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // 'Bearer ' 제거
            try {
                userEmail = jwtProvider.getUserEmail(token); // 토큰에서 이메일 추출
            } catch (ExpiredJwtException e) {
                // 토큰 만료 예외 처리
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token has expired");
                return;
            }
        }

        // SecurityContext에 인증 객체가 설정되지 않은 경우
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // JWT가 유효한 경우에만
            if (jwtProvider.validateToken(token)) {
                // 유저를 불러와서 Authentication 객체 생성
                try {
                    var userDetails = userDetailsService.loadUserByUsername(userEmail);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // SecurityContext에서 인증된 정보 가져오기
                    var authentication = SecurityContextHolder.getContext().getAuthentication();

                    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                        Users user = customUserDetails.getUser();
                        userLoginHistoryService.saveLoginHistory(user);  // 로그인 이력 저장
                    }

                } catch (UsernameNotFoundException e) {
                    // 인증 실패 시 처리
                    // 로그를 남기거나 예외를 처리
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token or User not found");
                    return;
                }
            }
        }
        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

}
