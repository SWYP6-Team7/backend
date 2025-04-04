package swyp.swyp6_team7.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import swyp.swyp6_team7.auth.service.JwtBlacklistService;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtBlacklistService jwtBlacklistService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserLoginHistoryService userLoginHistoryService;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();  // 매 테스트 실행 시 보안 컨텍스트 초기화
    }

    @Test
    public void testValidToken() throws ServletException, IOException {
        // Given: 유효한 토큰이 담긴 요청
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mocking 설정
        when(jwtProvider.getUserNumber("validToken")).thenReturn(1);
        when(jwtProvider.validateToken("validToken")).thenReturn(true);
        when(jwtBlacklistService.isTokenBlacklisted("validToken")).thenReturn(false);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("1")).thenReturn(userDetails);

        // When: 필터 실행
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then: 필터 체인이 계속 진행되고 SecurityContext에 인증 정보가 설정되어야 함
        verify(filterChain, times(1)).doFilter(request, response);
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        assert authentication.getPrincipal().equals(userDetails);
    }

    @Test
    public void testInvalidToken() throws ServletException, IOException {
        // Given: Mock HTTP request with invalid token
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalidToken");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock JwtProvider behavior
        when(jwtProvider.validateToken("invalidToken")).thenReturn(false);

        // When: Executing the filter
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then: Verify filter chain continues but no authentication is set
        verify(filterChain, times(1)).doFilter(request, response);
        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    public void testNoAuthorizationHeader() throws ServletException, IOException {
        // Given: Mock HTTP request without authorization header
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When: Executing the filter
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then: Verify filter chain continues but no authentication is set
        verify(filterChain, times(1)).doFilter(request, response);
        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    public void testUserNotFound() throws ServletException, IOException {
        // Given: 유효한 토큰이지만, 해당 사용자를 찾을 수 없는 경우
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.getUserNumber("validToken")).thenReturn(1);
        when(jwtProvider.validateToken("validToken")).thenReturn(true);
        when(jwtBlacklistService.isTokenBlacklisted("validToken")).thenReturn(false);
        when(userDetailsService.loadUserByUsername("1")).thenThrow(new UsernameNotFoundException("User not found"));

        // When: 필터 실행
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then: 응답 상태가 UNAUTHORIZED(401)로 설정되고 필터 체인은 진행되지 않아야 함
        assert response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED;
        verify(filterChain, never()).doFilter(request, response);
    }
}
