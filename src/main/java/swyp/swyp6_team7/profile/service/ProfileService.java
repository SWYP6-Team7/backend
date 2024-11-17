package swyp.swyp6_team7.profile.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.profile.repository.UserProfileRepository;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.profile.dto.ProfileUpdateRequest;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.UserTagPreference;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.UserTagPreferenceRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final UserTagPreferenceRepository userTagPreferenceRepository;


    @Transactional
    public void updateProfile(Integer userNumber,ProfileUpdateRequest request) {
        try {
            log.info("프로필 업데이트 시작 - userNumber: {}",userNumber);

            // Users 엔티티 업데이트
            Users user = userRepository.findUserWithTags(userNumber)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            user.setUserName(request.getName());
            user.setUserAgeGroup(AgeGroup.fromValue(request.getAgeGroup()));
            userRepository.save(user);

            // 선호 태그 업데이트
            if (request.getPreferredTags() != null && request.getPreferredTags().length > 0) {
                Set<UserTagPreference> tagPreferences = user.getTagPreferences();
                tagPreferences.clear();  // 기존 태그 삭제

                Set<Tag> preferredTags = new HashSet<>();

                for (String tagName : request.getPreferredTags()) {
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> {
                                Tag newTag = Tag.of(tagName);  // 태그가 없으면 새로 생성
                                tagRepository.save(newTag);
                                return newTag;
                            });

                    UserTagPreference userTagPreference = new UserTagPreference();
                    userTagPreference.setUser(user);
                    userTagPreference.setTag(tag);
                    tagPreferences.add(userTagPreference);  // 새로운 태그 추가
                    preferredTags.add(tag);
                }
                userTagPreferenceRepository.saveAll(tagPreferences);
            }
            // 영속성 컨텍스트를 강제로 flush하여 DB에 반영
            userRepository.flush();
            userTagPreferenceRepository.flush();
            log.info("프로필 수정 완료 - userNumber: {}",userNumber);
        } catch (Exception e){
            log.error("프로필 수정 중 에러발생 - userNumber: {}",userNumber,e);
            throw e;
        }
    }

    public Optional<Users> getUserByUserNumber(Integer userNumber) {
        try{
            log.info("사용자 프로필 가져오기 - userNumber: {}",userNumber);
            return userRepository.findUserWithTags(userNumber);
        } catch (Exception e){
            log.error("사용자 프로필 가져오는 중 에러 발생 - userNumber: {}",userNumber,e);
            throw e;
        }
    }
}
