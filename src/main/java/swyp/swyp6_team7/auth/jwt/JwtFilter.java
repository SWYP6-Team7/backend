package swyp.swyp6_team7.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final JwtBlacklistService jwtBlacklistService;

    /**
     * 로그인, 회원가입, 리프레시 토큰 요청은 필터 적용 제외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/api/login/oauth/google")
                || path.startsWith("/api/login/oauth/naver")
                || path.startsWith("/api/login/oauth/kakao")
                || path.equals("/api/login")
                || path.equals("/api/users/new")
                || path.equals("/api/token/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // 'Bearer ' 제거
            Integer userNumber;
            try {
                userNumber = jwtProvider.getUserNumber(token);
            } catch (ExpiredJwtException e) {
                // 토큰 만료 예외 처리
                log.warn("JWT 토큰 만료됨", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token has expired");
                return;
            } catch (JwtException e) {
                // 토큰 만료 예외 처리
                log.warn("JWT 토큰 유효하지 않음.", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }
            if (userNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtProvider.validateToken(token)) {
                    if (jwtBlacklistService.isTokenBlacklisted(token)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                        return;
                    }
                    try {
                        var userDetails = userDetailsService.loadUserByUsername(String.valueOf(userNumber));
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
                        }
                    } catch (UsernameNotFoundException e) {
                        // 인증 실패 시 처리
                        // 로그를 남기거나 예외를 처리
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token or User not found");
                        return;
                    }
                }
            }
        }
        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

}
