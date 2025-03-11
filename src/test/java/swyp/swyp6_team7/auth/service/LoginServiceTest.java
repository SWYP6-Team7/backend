package swyp.swyp6_team7.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.UserRole;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private LoginFacade loginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("로그인 성공 - 토큰 발급 성공")
    void testLoginSuccess() {
        String email = "test@example.com";
        String password = "password";
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(email);
        loginRequestDto.setPassword(password);

        Users user = Users.builder()
                .userEmail(email)
                .userPw(passwordEncoder.encode(password))
                .userSocialTF(false)
                .role(UserRole.USER)
                .build();

        List<String> roles = List.of(user.getRole().name());

        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getUserPw())).thenReturn(true); // 비밀번호 일치
        when(jwtProvider.createAccessToken(user.getUserNumber(), roles))
                .thenReturn("access-token");
        when(jwtProvider.createRefreshToken(user.getUserNumber())).thenReturn("refresh-token");

        LoginTokenResponse tokenResponse = loginService.login(loginRequestDto);

        assertNotNull(tokenResponse);
        assertEquals("access-token", tokenResponse.getAccessToken());
        assertEquals("refresh-token", tokenResponse.getRefreshToken());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 미존재")
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
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void testLoginFailureWrongPassword() {
        // Given
        String email = "test@example.com";
        String password = "password";
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(email);
        loginRequestDto.setPassword(password);

        Users user = Users.builder()
                .userEmail(email)
                .userPw("encodedPassword")
                .userSocialTF(false)
                .build();

        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getUserPw())).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> loginService.login(loginRequestDto));
    }

    @Test
    @DisplayName("getUserByEmail - 사용자 조회 성공")
    void testGetUserByEmailSuccess() {
        // Given
        String email = "test@example.com";
        Users user = new Users();
        user.setUserEmail(email);
        user.setUserNumber(1);

        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(user));

        // When
        Users result = loginService.getUserByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getUserNumber());
    }

    @Test
    @DisplayName("getUserByEmail - 사용자 조회 실패 (이메일 미존재)")
    void testGetUserByEmailFailure() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> loginService.getUserByEmail(email));
    }
}

