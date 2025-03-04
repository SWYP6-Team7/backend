package swyp.swyp6_team7.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 알파 환경에서 접근제어
@Component
@Slf4j
public class DevAuthenticationFilter extends OncePerRequestFilter {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private List<String> allowOrigins = List.of(
            "http://localhost:3000",
            "https://www.alpha.moing.io"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin"); // CORS 관련 요청의 출처
        String referer = request.getHeader("Referer"); // 페이지 이동 전 URL
        log.info("Origin: {}, Referer: {}", origin, referer);

        if (activeProfile.equals("dev")) {
            if (origin != null && !allowOrigins.contains(origin)) {
                log.info("Origin not allowed.");
                throw new ServletException("허용되지 않은 접근입니다.");
            }
        }

        filterChain.doFilter(request, response);
    }
}
