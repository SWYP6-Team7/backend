package swyp.swyp6_team7.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.UserRole;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        // Given
        String email = "test@example.com";
        String password = "password";
        String mockedAccessToken = "mockedAccessToken";

        Users mockUser = Users.builder()
                .userEmail(email)
                .userPw(passwordEncoder.encode(password))  // 실제로는 암호화된 비밀번호로 저장되어 있어야 합니다.
                .userSocialTF(false)  // 소셜 로그인이 아님
                .role(UserRole.USER)
                .build();

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(email);
        loginRequestDto.setPassword(password);

        List<String> roles = List.of(mockUser.getRole().name());

        when(userRepository.findByUserEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(password, mockUser.getUserPw())).thenReturn(true); // 비밀번호 일치
        when(jwtProvider.createAccessToken(mockUser.getUserNumber(), roles))
                .thenReturn(mockedAccessToken);


        // When

        Map<String, String> tokens = loginService.login(loginRequestDto);
        String accessToken = tokens.get("accessToken");



        // Then
        assertEquals(mockedAccessToken, accessToken);
    }

    @Test
    void testLoginFailureUserNotFound() {
        // Given
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("nonexistent@example.com");
        loginRequestDto.setPassword("password");

        when(userRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            loginService.login(loginRequestDto);
        });
        assertEquals("사용자 이메일을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void testLoginFailureWrongPassword() {
        // Given
        String email = "test@example.com";
        String password = "password";
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(email);
        loginRequestDto.setPassword(password);

        Users mockUser = Users.builder()
                .userEmail(email)
                .userPw("encodedPassword")
                .userSocialTF(false)
                .build();

        when(userRepository.findByUserEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false); // 비밀번호가 일치하지 않음

        // When & Then
        assertThrows(BadCredentialsException.class, () -> loginService.login(loginRequestDto));
    }
}

