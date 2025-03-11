package swyp.swyp6_team7.auth.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.global.exception.ErrorCode;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.exception.UserBlockException;
import swyp.swyp6_team7.member.entity.SocialUsers;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.service.UserLoginHistoryService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginFacade {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final UserLoginHistoryService userLoginHistoryService;
    private final MemberService memberService;
    private final SocialLoginService socialLoginService;

    public String getCookie(String refreshToken) {
        // 리프레시 토큰을 HttpOnly 쿠키로 설정 (TTL 7일)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(tokenService.getRefreshTokenValidity())
                .build();

        return cookie.toString();
    }

    private void checkUserCanLogin(Users user, String password) {
        if (!passwordEncoder.matches(password, user.getUserPw())) {
            log.warn("로그인 실패 - 비밀번호 불일치: email={}", user.getUserEmail());
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getUserSocialTF()) {
            log.warn("로그인 실패 - 소셜 로그인 사용자: email={}", user.getUserEmail());
            throw new IllegalArgumentException("간편 로그인으로 가입된 계정입니다. 소셜 로그인으로 접속해 주세요.");
        }

        checkUserIsBlocked(user);
    }

    public LoginTokenResponse socialLogin(String socialLoginId, String email) {
        // 소셜 사용자 정보 확인
        Users user = getUserBySocialInfo(socialLoginId, email);

        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(
                user.getUserNumber(),
                List.of(user.getRole().name())
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getUserNumber());
        log.info("JWT 토큰 생성 완료: accessToken={}, refreshToken=****", accessToken);

        processUserLoginEvent(user);

        return new LoginTokenResponse(user.getUserNumber(), accessToken, refreshToken);
    }

    public LoginTokenResponse login(LoginRequestDto loginRequestDto) {
        log.info("로그인 시도: email={}", loginRequestDto.getEmail());

        Users user = userRepository.findByUserEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 이메일을 찾을 수 없음: email={}", loginRequestDto.getEmail());
                    return new UsernameNotFoundException("사용자 이메일을 찾을 수 없습니다.");
                });

        checkUserCanLogin(user, loginRequestDto.getPassword());

        String accessToken = jwtProvider.createAccessToken(user.getUserNumber(), List.of(user.getRole().name()));
        String refreshToken = jwtProvider.createRefreshToken(user.getUserNumber());

        // 로그인 성공 후 Redis에 RefreshToken 저장
        Integer userNumber = user.getUserNumber();
        tokenService.storeRefreshToken(userNumber, refreshToken);

        processUserLoginEvent(user);

        return new LoginTokenResponse(user.getUserNumber(), accessToken, refreshToken);
    }

    private void processUserLoginEvent(Users user) {
        // 로그인 이력 저장
        userLoginHistoryService.saveLoginHistory(user);
        memberService.updateLoginDate(user);  // 로그인 시간 업데이트
    }

    private void checkUserIsBlocked(Users user) {
        if (user.isBlocked()) {
            log.info("신고에 의해 정지된 계정입니다. userNumber:{}", user.getUserNumber());
            throw new UserBlockException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    // 이메일로 유저를 조회하는 메서드 추가
    public Users getUserByEmail(String email) {
        log.info("이메일로 사용자 조회 시도: email={}", email);

        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    }

    public Users getUserBySocialInfo(String socialLoginId, String email) {
        Optional<SocialUsers> socialUserOpt = socialLoginService.findSocialUserByLoginId(socialLoginId);

        if (socialUserOpt.isEmpty()) {
            log.info("존재하지 않는 소셜 로그인 유저입니다. socialLoginId: {}", socialLoginId);
            throw new UsernameNotFoundException("존재하지 않는 유저입니다.");
        }

        SocialUsers socialUser = socialUserOpt.get();

        // 삭제된 유저인지 확인
        Users user = socialUser.getUser();
        if (user.getUserStatus() == UserStatus.DELETED) {
            log.info("소셜 로그인 실패 - 삭제된 계정: userNumber={}, email={}", user.getUserNumber(), user.getUserEmail());
            throw new MoingApplicationException("해당 계정은 탈퇴된 상태입니다. 자세한 사항은 관리자에 문의하세요.");
        }

        if (!Objects.equals(user.getUserEmail(), email)) {
            log.info("소셜 로그인 실패 - 일치하지 않는 이메일. userNumber={}, email={}", user.getUserNumber(), user.getUserEmail());
            throw new MoingApplicationException("계정 정보가 일치하지 않습니다. 이메일을 확인해주세요");
        }

        return user;
    }

    public Integer getUserNumberByEmail(String email) {
        return getUserByEmail(email).getUserNumber();
    }
}
