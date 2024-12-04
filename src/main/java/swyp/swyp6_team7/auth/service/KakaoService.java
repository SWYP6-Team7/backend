package swyp.swyp6_team7.auth.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import swyp.swyp6_team7.auth.dto.SignupRequestDto;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.auth.provider.KakaoProvider;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.SocialUserRepository;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberDeletedService;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.UserTagPreference;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.UserTagPreferenceRepository;
import swyp.swyp6_team7.tag.service.TagService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    private final KakaoProvider kakaoProvider;
    private final UserRepository userRepository;
    private final SocialUserRepository socialUserRepository;
    private final JwtProvider jwtProvider;
    private final MemberDeletedService memberDeletedService;
    private final TagRepository tagRepository;
    private final UserTagPreferenceRepository userTagPreferenceRepository;

    public Map<String, String> getUserInfoFromKakao(String code) {
        log.info("Kakao 사용자 정보 요청: code={}", code);
        return kakaoProvider.getUserInfoFromKakao(code);
    }

    @Transactional
    public Map<String, String> processKakaoLogin(String code) {
        log.info("Kakao 사용자 정보 수집 및 저장 시작: code={}", code);

        try {
            Map<String, String> userInfo = kakaoProvider.getUserInfoFromKakao(code);
            Users user = saveSocialUser(userInfo);

            Map<String, String> response = new HashMap<>();
            response.put("userNumber", user.getUserNumber().toString());
            response.put("userName", user.getUserName());
            response.put("userEmail", user.getUserEmail());
            response.put("userStatus", user.getUserStatus().toString());
            response.put("socialLoginId", userInfo.get("socialLoginId"));

            log.info("Kakao 사용자 정보 수집 및 저장 완료: userNumber={}", user.getUserNumber());
            return response;
        } catch (Exception e) {
            log.error("Kakao 사용자 정보 수집 중 오류 발생", e);
            throw new RuntimeException("Failed to process Kakao login", e);
        }

    }

    @Transactional
    public Map<String, String> completeSignup(@RequestBody SignupRequestDto signupData) {
        log.info("Kakao 회원가입 완료 요청: userNumber={}", signupData.getUserNumber());

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

            user.setUserEmail(signupData.getEmail());
            user.setUserGender(Gender.valueOf(signupData.getGender().toUpperCase()));
            user.setUserAgeGroup(AgeGroup.fromValue(signupData.getAgeGroup()));
            user.setUserStatus(UserStatus.ABLE);

            if (signupData.getPreferredTags() != null && !signupData.getPreferredTags().isEmpty()) {
                Set<String> preferredTags = signupData.getPreferredTags();
                log.info("사용자 선호 태그 처리: userNumber={}, tags={}", user.getUserNumber(), preferredTags);

                // 선호 태그 저장 로직
                Set<UserTagPreference> tagPreferences = new HashSet<>();

                for (String tagName : preferredTags) {
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> {
                                Tag newTag = Tag.of(tagName);
                                tagRepository.save(newTag);
                                return newTag;
                            });

                    // 사용자와 태그 연결
                    UserTagPreference userTagPreference = new UserTagPreference();
                    userTagPreference.setUser(user);
                    userTagPreference.setTag(tag);
                    tagPreferences.add(userTagPreference);
                }

                // 저장
                userTagPreferenceRepository.saveAll(tagPreferences);
                log.info("사용자 선호 태그 저장 완료: userNumber={}, tagCount={}", user.getUserNumber(), tagPreferences.size());
            }

            userRepository.save(user);

            Optional<SocialUsers> existingSocialUser = socialUserRepository.findByUser(user);
            String socialLoginId = existingSocialUser.map(SocialUsers::getSocialLoginId).orElse("N/A");
            if (existingSocialUser.isPresent()) {
                SocialUsers socialUser = existingSocialUser.get();
                socialUser.setSocialEmail(signupData.getEmail());
                socialUserRepository.save(socialUser);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Signup complete");
            response.put("socialLoginId", socialLoginId);
            response.put("email", user.getUserEmail());

            log.info("Kakao 회원가입 완료 성공: userNumber={}", user.getUserNumber());
            return response;
        } catch (Exception e) {
            log.error("Kakao 회원가입 완료 처리 중 오류 발생", e);
            throw new RuntimeException("Failed to complete signup", e);
        }
    }

    @Transactional
    private Users saveSocialUser(Map<String, String> userInfo) {
        String email = userInfo.getOrDefault("email", "unknown@" + UUID.randomUUID().toString().substring(0, 8) + ".email");
        String socialLoginId = userInfo.get("socialLoginId");
        String nickname = userInfo.get("nickname");

        log.info("소셜 사용자 저장 시작: email={}, socialLoginId={}", email, socialLoginId);

        try {
            // 재가입 제한 검증
            memberDeletedService.validateReRegistration(email);
            log.info("재가입 제한 검증 통과: email={}", email);

            Optional<SocialUsers> existingSocialUser = socialUserRepository.findBySocialLoginId(socialLoginId);
            if (existingSocialUser.isPresent()) {
                return existingSocialUser.get().getUser();
            } else {
                Users newUser = new Users();
                newUser.setUserEmail(email);
                newUser.setUserName(nickname != null ? nickname : "Unknown");
                newUser.setUserPw("social-login");
                newUser.setUserGender(Gender.NULL);
                newUser.setUserAgeGroup(AgeGroup.UNKNOWN);
                newUser.setUserSocialTF(true);
                newUser.setUserStatus(UserStatus.PENDING);
                log.info("새로운 소셜 사용자 생성: email={}", email);
                newUser = userRepository.save(newUser);

                if (!socialUserRepository.existsBySocialLoginId(socialLoginId)) {
                    SocialUsers socialUser = new SocialUsers();
                    socialUser.setUser(newUser);
                    socialUser.setSocialLoginId(socialLoginId);
                    socialUser.setSocialEmail(email);
                    socialUser.setSocialProvider(SocialProvider.KAKAO);
                    socialUserRepository.save(socialUser);
                    log.info("SocialUsers 테이블에 소셜 사용자 정보 저장 완료: socialLoginId={}", socialLoginId);
                }

                return newUser;
            }
        } catch (Exception e) {
            log.error("소셜 사용자 저장 중 오류 발생", e);
            throw new RuntimeException("Failed to save social user", e);
        }
    }
}
