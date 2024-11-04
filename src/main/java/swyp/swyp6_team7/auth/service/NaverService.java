package swyp.swyp6_team7.auth.service;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.auth.provider.NaverProvider;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.SocialUserRepository;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NaverService {

    private final NaverProvider naverProvider;
    private final UserRepository userRepository;
    private final SocialUserRepository socialUserRepository;
    @Autowired
    public NaverService(NaverProvider naverProvider, UserRepository userRepository, SocialUserRepository socialUserRepository) {
        this.naverProvider = naverProvider;
        this.userRepository = userRepository;
        this.socialUserRepository = socialUserRepository;
    }
    // 네이버 로그인 URL 생성
    public String naverLogin() {
        String state = UUID.randomUUID().toString(); // 무작위 state 값 생성
        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id="
                + naverProvider.getClientId() + "&redirect_uri=" + naverProvider.getRedirectUri() + "&state=" + state;

        log.info("Naver 로그인 URL 생성: {}", naverAuthUrl);
        return naverAuthUrl;
    }

    public Map<String, String> getUserInfoFromNaver(String code, String state) {
        log.info("Naver 사용자 정보 요청: code={}, state={}", code, state);

        try {
            return naverProvider.getUserInfoFromNaver(code, state);
        } catch (Exception e) {
            log.error("Naver 사용자 정보 요청 중 오류 발생: code={}, state={}", code, state, e);
            throw new RuntimeException("Failed to get user info from Naver", e);
        }
    }
    public Map<String, String> processNaverLogin(String code, String state) {
        log.info("Naver 로그인 처리 시작: code={}, state={}", code, state);

        try {
            Map<String, String> userInfo = naverProvider.getUserInfoFromNaver(code, state);
            saveSocialUser(
                    userInfo.get("email"),
                    userInfo.get("name"),
                    userInfo.get("gender"),
                    userInfo.get("socialID"),
                    userInfo.get("ageGroup"),
                    userInfo.get("provider")
            );

            log.info("Naver 로그인 처리 성공: userInfo={}", userInfo);
            return userInfo;
        } catch (Exception e) {
            log.error("Naver 로그인 처리 중 오류 발생: code={}, state={}", code, state, e);
            throw new RuntimeException("Failed to process Naver login", e);
        }
    }

    // Users와 SocialUsers에 저장하는 메서드
    @Transactional
    private void saveSocialUser(String email, String name, String gender, String socialLoginId, String ageGroup, String provider) {
        log.info("소셜 사용자 저장 시작: email={}, socialLoginId={}", email, socialLoginId);

        try {
            Optional<Users> existingUser = userRepository.findByUserEmail(email);

            Users user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                log.info("기존 사용자 발견: email={}", email);
            } else {
                user = new Users();
                user.setUserEmail(email);
                user.setUserName(name);
                user.setUserPw("social-login");
                if (gender != null && !gender.isEmpty()) {
                    user.setUserGender(Gender.valueOf(gender.toUpperCase()));
                }
                user.setUserAgeGroup(AgeGroup.fromValue(ageGroup));
                user.setUserSocialTF(true);
                user.setUserStatus(UserStatus.ABLE);
                user.setRole(UserRole.USER);

                user = userRepository.save(user);
                log.info("새 사용자 저장 성공: email={}", email);
            }

            if (!socialUserRepository.existsBySocialLoginId(socialLoginId)) {
                SocialUsers socialUser = new SocialUsers();
                socialUser.setUser(user);
                socialUser.setSocialLoginId(socialLoginId);
                socialUser.setSocialEmail(email);
                socialUser.setSocialProvider(SocialProvider.fromString(provider));

                socialUserRepository.save(socialUser);
                log.info("소셜 사용자 정보 저장 성공: socialLoginId={}", socialLoginId);
            }
        } catch (Exception e) {
            log.error("소셜 사용자 저장 중 오류 발생: email={}, socialLoginId={}", email, socialLoginId, e);
            throw new RuntimeException("Failed to save social user", e);
        }

    }
}
