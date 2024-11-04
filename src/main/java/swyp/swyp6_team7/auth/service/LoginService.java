package swyp.swyp6_team7.auth.service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public  Map<String, String> login(LoginRequestDto loginRequestDto) {
        log.info("로그인 시도: email={}", loginRequestDto.getEmail());

        try {
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

            // Access Token 생성
            String accessToken = jwtProvider.createAccessToken(user.getUserEmail(), user.getUserNumber(), List.of(user.getRole().name()));
            // Refresh Token 생성
            String refreshToken = jwtProvider.createRefreshToken(user.getUserEmail(), user.getUserNumber());

            log.info("로그인 성공: userNumber={}, email={}", user.getUserNumber(), user.getUserEmail());

            // Access Token과 Refresh Token을 Map에 담아 반환
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("accessToken", accessToken);
            tokenMap.put("refreshToken", refreshToken);

            return tokenMap;

        } catch (BadCredentialsException | UsernameNotFoundException | IllegalArgumentException e) {
            log.error("로그인 중 오류 발생: email={}, message={}", loginRequestDto.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("알 수 없는 오류로 로그인 실패: email={}", loginRequestDto.getEmail(), e);
            throw new RuntimeException("로그인에 실패했습니다.", e);
        }
    }
    // 이메일로 유저를 조회하는 메서드 추가
    public Users getUserByEmail(String email) {
        log.info("이메일로 사용자 조회 시도: email={}", email);

        try {
            return userRepository.findByUserEmail(email)
                    .orElseThrow(() -> {
                        log.warn("사용자 조회 실패 - 이메일을 찾을 수 없음: email={}", email);
                        return new UsernameNotFoundException("User not found with email: " + email);
                    });
        } catch (UsernameNotFoundException e) {
            log.error("사용자 조회 중 오류 발생: email={}", email, e);
            throw e;
        }

    }
}
