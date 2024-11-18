package swyp.swyp6_team7.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import swyp.swyp6_team7.auth.dto.SignupRequestDto;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.provider.GoogleProvider;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.SocialUserRepository;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberDeletedService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleService {
    private final GoogleProvider googleProvider;
    private final UserRepository userRepository;
    private final SocialUserRepository socialUserRepository;
    private final JwtProvider jwtProvider;
    private final MemberDeletedService memberDeletedService;

    public Map<String, String> getUserInfoFromGoogle(String code) {
        log.info("Google 사용자 정보 요청: code={}", code);
        return googleProvider.getUserInfoFromGoogle(code);
    }

    @Transactional
    public Map<String, String> processGoogleLogin(String code) {
        log.info("Google 사용자 정보 수집 및 저장 시작: code={}", code);
        try {
            Map<String, String> userInfo = googleProvider.getUserInfoFromGoogle(code);
            Users user = saveSocialUser(userInfo);

            Map<String, String> response = new HashMap<>();
            response.put("userNumber", user.getUserNumber().toString());
            response.put("userName", user.getUserName());
            response.put("userEmail", user.getUserEmail());
            response.put("userStatus", user.getUserStatus().toString());
            response.put("socialLoginId", userInfo.get("socialLoginId"));

            log.info("Google 사용자 정보 수집 및 저장 완료: userNumber={}", user.getUserNumber());
            return response;
        } catch (Exception e) {
            log.error("Google 사용자 정보 수집 중 오류 발생", e);
            throw new RuntimeException("Failed to process Google login", e);
        }
    }

    @Transactional
    public Map<String, String> completeSignup(@RequestBody SignupRequestDto signupData) {
        log.info("Google 회원가입 완료 요청: userNumber={}", signupData.getUserNumber());
        try {
            if (signupData.getUserNumber() == null) {
                log.warn("회원가입 시 userNumber가 제공되지 않았습니다.");
                throw new IllegalArgumentException("User ID must not be null");
            }

            Users user = userRepository.findById(signupData.getUserNumber())
                    .orElseThrow(() -> {
                        log.warn("회원가입 완료 처리 중 사용자를 찾을 수 없음: userNumber={}", signupData.getUserNumber());
                        return new IllegalArgumentException("User not found");
                    });

            user.setUserGender(Gender.valueOf(signupData.getGender().toUpperCase()));
            user.setUserAgeGroup(AgeGroup.fromValue(signupData.getAgeGroup()));
            user.setUserStatus(UserStatus.ABLE);

            if (signupData.getPreferredTags() != null) {
                Set<String> preferredTags = signupData.getPreferredTags();
                log.info("사용자 선호 태그 처리: userNumber={}, tags={}", user.getUserNumber(), preferredTags);
                // 선호 태그 저장 로직 추가 가능
            }

            userRepository.save(user);

            Optional<SocialUsers> existingSocialUser = socialUserRepository.findByUser(user);
            String socialLoginId = existingSocialUser.map(SocialUsers::getSocialLoginId).orElse("N/A");
            if (existingSocialUser.isPresent()) {
                socialUserRepository.save(existingSocialUser.get());
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Signup complete");
            response.put("socialLoginId", socialLoginId);
            response.put("email", user.getUserEmail());

            log.info("Google 회원가입 완료 성공: userNumber={}", user.getUserNumber());
            return response;
        } catch (Exception e) {
            log.error("Google 회원가입 완료 처리 중 오류 발생", e);
            throw new RuntimeException("Failed to complete signup", e);
        }
    }

    @Transactional
    private Users saveSocialUser(Map<String, String> userInfo) {
        String email = userInfo.get("email");
        String socialLoginId = userInfo.get("socialLoginId");
        log.info("소셜 사용자 저장 시작: email={}, socialLoginId={}", email, socialLoginId);

        try {
            // 재가입 제한 검증
            memberDeletedService.validateReRegistration(email);
            log.info("재가입 제한 검증 통과: email={}", email);

            Optional<Users> existingUser = userRepository.findByUserEmail(email);

            Users user;
            if (existingUser.isPresent()) {
                // 기존 사용자 처리
                user = existingUser.get();
                log.info("기존 사용자 발견: email={}, userNumber={}", email, user.getUserNumber());
            } else {
                user = new Users();
                user.setUserEmail(email);
                user.setUserName(userInfo.getOrDefault("name", "Unknown"));
                user.setUserPw("social-login"); // 소셜 로그인 사용자의 기본 비밀번호 처리
                user.setUserGender(Gender.NULL);
                user.setUserAgeGroup(AgeGroup.UNKNOWN);
                user.setUserSocialTF(true);
                user.setUserStatus(UserStatus.PENDING); // 기본 상태 설정
                user.setRole(UserRole.USER); // 일반 사용자 역할 설정

                user = userRepository.save(user);
                log.info("새 소셜 사용자 저장 성공: email={}, userNumber={}", email, user.getUserNumber());
            }

            if (!socialUserRepository.existsBySocialLoginId(socialLoginId)) {
                SocialUsers socialUser = new SocialUsers();
                socialUser.setUser(user);
                socialUser.setSocialLoginId(socialLoginId);
                socialUser.setSocialEmail(email);
                socialUser.setSocialProvider(SocialProvider.GOOGLE);
                socialUserRepository.save(socialUser);
                log.info("SocialUsers 테이블에 소셜 사용자 정보 저장 완료: socialLoginId={}", socialLoginId);
            } else {
                log.info("이미 저장된 소셜 사용자: socialLoginId={}", socialLoginId);
            }

            return user;
        } catch (IllegalArgumentException e) {
            log.warn("소셜 사용자 저장 실패 - 재가입 제한: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("소셜 사용자 저장 중 오류 발생", e);
            throw new RuntimeException("Failed to save social user", e);
        }

    }
}
