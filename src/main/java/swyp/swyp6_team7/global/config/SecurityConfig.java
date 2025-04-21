package swyp.swyp6_team7.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import swyp.swyp6_team7.auth.filter.DevAuthenticationFilter;
import swyp.swyp6_team7.auth.jwt.JwtFilter;

import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final DevAuthenticationFilter devAuthenticationFilter;

    public SecurityConfig(JwtFilter jwtFilter, DevAuthenticationFilter devAuthenticationFilter) {
        this.jwtFilter = jwtFilter;
        this.devAuthenticationFilter = devAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll() // 모든 사용자 조회 가능
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN") // POST는 관리자만 가능
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasRole("ADMIN") // PUT은 관리자만 가능
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasRole("ADMIN") // DELETE는 관리자만 가능

                        // 마이페이지 관련 권한 설정
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/logout").authenticated()

                        // 비회원(비로그인) 관련 권한 설정
                        .requestMatchers(
                                "/api/travel/**",
                                "/api/travels/recent",
                                "/api/travels/recommend",
                                "/api/travels/search",
                                "/api/community/posts",
                                "/api/community/posts/{postNumber}",
                                "/api/{relatedType}/{relatedNumber}/comments",
                                "/api/autocomplete",
                                "/api/users/*/profile"
                        ).permitAll()

                        // 기타 경로
                        .requestMatchers(
                                "/api/admins/new",
                                "/api/login",
                                "/api/users/new",
                                "/api/users/sign-up",
                                "/api/token/refresh",
                                "/api/social/login",
                                "/api/social/kakao/complete-signup",
                                "/api/social/google/complete-signup",
                                "/api/login/oauth/kakao/**", "/api/login/oauth/naver/**", "/api/login/oauth/google/**",
                                "/error",
                                "/api/users-email",
                                "/actuator/health", // Health check endpoint permission
                                "/api/community/images/**",
                                "/api/community/*/images",
                                "/api/inquiry/submit",
                                "/api/member/block/my/detail" // 내 계정 차단내용 조회
                        ).permitAll()

                        .requestMatchers(
                                "/api/verify/email/**"
                        ).permitAll() // 이메일 인증

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/health"
                        ).permitAll()

                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .addFilterBefore(devAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        List<String> allowedOrigins = List.of(
                "https://release-back.vercel.app",
                "https://www.moing.shop",
                "https://www.moing.io",
                "https://www.alpha.moing.io"
        );

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}