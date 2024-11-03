package swyp.swyp6_team7.auth.service;

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
        // SocialUsers 테이블에서 사용자 정보 가져오기
        Optional<SocialUsers> optionalSocialUser = socialUserRepository.findBySocialLoginIdAndSocialEmail(socialLoginId, email);

        if (optionalSocialUser.isEmpty()) {
            throw new IllegalArgumentException("User not found in the database with the given social_login_id and email.");
        }

        SocialUsers socialUser = optionalSocialUser.get();
        return socialUser.getUser();  // 연결된 Users 정보 반환
    }

    private SocialLoginProvider getProvider(String provider) {
        return socialLoginProviders.stream()
                .filter(p -> p.supports(provider))  // 각 제공자 클래스에서 provider를 지원하는지 체크
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + provider));
    }
    // 새로운 사용자를 저장하거나 기존 사용자 반환
    private Users processUser(Map<String, String> userInfo){
        String socialLoginId = userInfo.get("socialNumber");

        Optional<SocialUsers> existingSocialUser = socialUserRepository.findBySocialLoginId(socialLoginId);
        if (existingSocialUser.isPresent()) {
            return existingSocialUser.get().getUser();
        }

        Users user = createUserFromInfo(userInfo);
        user = userRepository.save(user);

        saveSocialUser(userInfo, user);

        return user;
    }
    // SocialUsers 엔티티에 소셜 사용자 정보 저장
    private void saveSocialUser(Map<String, String> userInfo, Users user) {
        String socialLoginId = userInfo.get("socialNumber");

        // 소셜 고유 ID로 중복 확인하여 없을 때만 저장
        Optional<SocialUsers> existingSocialUser = socialUserRepository.findBySocialLoginId(socialLoginId);
        if (existingSocialUser.isEmpty()) {
            SocialUsers socialUser = SocialUsers.builder()
                    .socialLoginId(socialLoginId)
                    .socialEmail(userInfo.get("email"))
                    .user(user)
                    .socialProvider(SocialProvider.fromString(userInfo.get("provider")))
                    .build();

            socialUserRepository.save(socialUser);
        }
    }
    // 새로운 Users 엔티티 생성
    private Users createUserFromInfo(Map<String, String> userInfo) {
        Users user = Users.builder()
                .userEmail(userInfo.get("email"))
                .userName(userInfo.get("name") != null ? userInfo.get("name") : "Unknown")
                .userPw("social-login")  // 소셜 로그인 사용자는 패스워드 불필요
                .userStatus(UserStatus.ABLE)
                .userSocialTF(true)  // 소셜 로그인 여부 설정
                .userRegDate(LocalDateTime.now())
                .build();

        // 성별 처리 부분 추가
        String gender = userInfo.get("gender");
        if (gender != null) {
            try {
                user.setUserGender(Gender.valueOf(gender.toUpperCase()));  // 'M' 또는 'F'를 ENUM으로 변환
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 성별 값입니다: " + gender);  // 잘못된 값 처리
            }
        } else {
            throw new IllegalArgumentException("성별 값이 없습니다.");  // 성별이 없으면 예외 처리
        }

        // 연령대 처리 (age 값에서 AgeGroup으로 변환)
        String ageGroup = userInfo.get("ageGroup");
        if (ageGroup != null) {
            user.setUserAgeGroup(convertToAgeGroup(ageGroup));  // 연령대 변환 메서드 사용
        } else {
            throw new IllegalArgumentException("연령대 값이 없습니다.");
        }

        return user;
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
}

