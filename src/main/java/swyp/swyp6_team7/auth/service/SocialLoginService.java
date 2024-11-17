package swyp.swyp6_team7.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import swyp.swyp6_team7.auth.provider.SocialLoginProvider;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.SocialUserRepository;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
public class SocialLoginService {
    private final UserRepository userRepository;
    private final SocialUserRepository socialUserRepository;
    private final List<SocialLoginProvider> socialLoginProviders;

    public SocialLoginService(UserRepository userRepository,
                              SocialUserRepository socialUserRepository,
                              List<SocialLoginProvider> socialLoginProviders) {
        this.userRepository = userRepository;
        this.socialUserRepository = socialUserRepository;
        this.socialLoginProviders = socialLoginProviders;
    }

    @Transactional
    public Users handleSocialLogin(String socialLoginId, String email) {
        log.info("소셜 로그인 처리 시작: socialLoginId={}, email={}", socialLoginId, email);

        try {
            Optional<SocialUsers> optionalSocialUser = socialUserRepository.findBySocialLoginIdAndSocialEmail(socialLoginId, email);

            if (optionalSocialUser.isEmpty()) {
                log.warn("소셜 로그인 실패 - 데이터베이스에서 사용자 찾을 수 없음: socialLoginId={}, email={}", socialLoginId, email);
                throw new IllegalArgumentException("User not found in the database with the given social_login_id and email.");
            }

            SocialUsers socialUser = optionalSocialUser.get();
            log.info("소셜 로그인 성공: userNumber={}", socialUser.getUser().getUserNumber());
            return socialUser.getUser();
        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류 발생: socialLoginId={}, email={}", socialLoginId, email, e);
            throw e;
        }
    }

    private SocialLoginProvider getProvider(String provider) {
        log.info("소셜 로그인 제공자 검색 시작: provider={}", provider);

        return socialLoginProviders.stream()
                .filter(p -> p.supports(provider))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("지원되지 않는 소셜 제공자: provider={}", provider);
                    return new IllegalArgumentException("Unsupported provider: " + provider);
                });
    }
    // 새로운 사용자를 저장하거나 기존 사용자 반환
    private Users processUser(Map<String, String> userInfo){

        String email = userInfo.get("email");
        log.info("사용자 처리 시작: email={}", email);

        try {
            Optional<Users> existingUserOpt = userRepository.findByUserEmail(email);
            Users user;
            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                log.info("기존 사용자 발견: email={}", email);
            } else {
                user = createUserFromInfo(userInfo);
                user = userRepository.save(user);
                log.info("새 사용자 저장 성공: email={}", email);
            }
            return user;
        } catch (Exception e) {
            log.error("사용자 처리 중 오류 발생: email={}", email, e);
            throw new RuntimeException("Failed to process user", e);
        }

    }
    // SocialUsers 엔티티에 소셜 사용자 정보 저장
    private void saveSocialUser(Map<String, String> userInfo, Users user) {
        String socialLoginId = userInfo.get("socialNumber");

        log.info("소셜 사용자 정보 저장 시작: socialLoginId={}", socialLoginId);


        try {
            Optional<SocialUsers> existingSocialUser = socialUserRepository.findBySocialLoginId(socialLoginId);
            if (existingSocialUser.isEmpty()) {
                SocialUsers socialUser = SocialUsers.builder()
                        .socialLoginId(socialLoginId)
                        .socialEmail(userInfo.get("email"))
                        .user(user)
                        .socialProvider(SocialProvider.fromString(userInfo.get("provider")))
                        .build();

                socialUserRepository.save(socialUser);
                log.info("소셜 사용자 정보 저장 성공: socialLoginId={}", socialLoginId);
            }
        } catch (Exception e) {
            log.error("소셜 사용자 정보 저장 중 오류 발생: socialLoginId={}", socialLoginId, e);
            throw new RuntimeException("Failed to save social user information", e);
        }
    }
    // 새로운 Users 엔티티 생성
    private Users createUserFromInfo(Map<String, String> userInfo) {
        log.info("새 사용자 생성 시작: email={}", userInfo.get("email"));

        try {
            Users user = Users.builder()
                    .userEmail(userInfo.get("email"))
                    .userName(userInfo.getOrDefault("name", "Unknown"))
                    .userPw("social-login")
                    .userStatus(UserStatus.ABLE)
                    .userSocialTF(true)
                    .userRegDate(LocalDateTime.now())
                    .build();

            String gender = userInfo.get("gender");
            if (gender != null) {
                try {
                    user.setUserGender(Gender.valueOf(gender.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("유효하지 않은 성별 값: gender={}", gender, e);
                    throw new IllegalArgumentException("유효하지 않은 성별 값입니다: " + gender);
                }
            } else {
                log.warn("성별 값이 없음");
                throw new IllegalArgumentException("성별 값이 없습니다.");
            }

            String ageGroup = userInfo.get("ageGroup");
            if (ageGroup != null) {
                user.setUserAgeGroup(convertToAgeGroup(ageGroup));
            } else {
                log.warn("연령대 값이 없음");
                throw new IllegalArgumentException("연령대 값이 없습니다.");
            }

            return user;
        } catch (Exception e) {
            log.error("사용자 생성 중 오류 발생: email={}", userInfo.get("email"), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    private AgeGroup convertToAgeGroup(String ageRange) {
        if (ageRange.startsWith("10")) {
            return AgeGroup.TEEN;
        } else if (ageRange.startsWith("20")) {
            return AgeGroup.TWENTY;
        } else if (ageRange.startsWith("30")) {
            return AgeGroup.THIRTY;
        } else if (ageRange.startsWith("40")) {
            return AgeGroup.FORTY;
        } else if (ageRange.startsWith("50")) {
            return AgeGroup.FIFTY_PLUS;
        } else {
            throw new IllegalArgumentException("유효하지 않은 연령대 값입니다: " + ageRange);
        }
    }
    public Optional<SocialUsers> findSocialUserByLoginId(String socialLoginId) {
        return socialUserRepository.findBySocialLoginId(socialLoginId);
    }
}

