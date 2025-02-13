package swyp.swyp6_team7.auth.service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;


    public Map<String, String> login(LoginRequestDto loginRequestDto) {
        log.info("로그인 시도: email={}", loginRequestDto.getEmail());

        Users user = userRepository.findByUserEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 이메일을 찾을 수 없음: email={}", loginRequestDto.getEmail());
                    return new UsernameNotFoundException("사용자 이메일을 찾을 수 없습니다.");
                });

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getUserPw())) {
            log.warn("로그인 실패 - 비밀번호 불일치: email={}", loginRequestDto.getEmail());
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getUserSocialTF()) {
            log.warn("로그인 실패 - 소셜 로그인 사용자: email={}", loginRequestDto.getEmail());
            throw new IllegalArgumentException("간편 로그인으로 가입된 계정입니다. 소셜 로그인으로 접속해 주세요.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserNumber(), List.of(user.getRole().name()));
        String refreshToken = jwtProvider.createRefreshToken(user.getUserNumber());

        // Access Token과 Refresh Token을 Map에 담아 반환
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);

        return tokenMap;
    }

    // 이메일로 유저를 조회하는 메서드 추가
    public Users getUserByEmail(String email) {
        log.info("이메일로 사용자 조회 시도: email={}", email);

        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    }

    public Integer getUserNumberByEmail(String email) {
        return getUserByEmail(email).getUserNumber();
    }
}
