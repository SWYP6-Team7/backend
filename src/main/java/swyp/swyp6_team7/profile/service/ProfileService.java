package swyp.swyp6_team7.profile.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.profile.dto.TargetUserProfileResponse;
import swyp.swyp6_team7.profile.dto.ProfileViewResponse;
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
import swyp.swyp6_team7.travel.service.TravelAppliedService;
import swyp.swyp6_team7.travel.service.TravelListService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final UserTagPreferenceRepository userTagPreferenceRepository;
    private final TravelAppliedService travelAppliedService;
    private final TravelListService travelListService;


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

    // 상대방 프로필 조회 메서드 (TargetUserProfileResponse 사용)
    public TargetUserProfileResponse getOtherUserProfile(Integer userNumber) {
        try {
            log.info("상대방 프로필 조회 시작 - userNumber: {}", userNumber);
            Users user = userRepository.findUserWithTags(userNumber)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Integer createdTravelCount = travelListService.countCreatedTravelsByUser(userNumber);      // 사용자가 만든 여행 개수
            Integer participatedTravelCount = travelAppliedService.countAppliedTrpsByUser(userNumber); // 사용자가 참가한 여행 개수

            // 추가 정보는 아직 구현되지 않았으므로 기본값을 사용합니다.
            Double travelDistance = 0.0; // 계산된 여행 거리
            Integer visitedCountryCount = 0; // 방문한 국가 수
            Integer travelBadgeCount = 0; // 획득한 여행 뱃지 수

            Boolean recentlyReported = false;    // 최근 신고 여부
            Integer totalReportCount = 0;        // 누적 신고 횟수

            return new TargetUserProfileResponse(
                    user,
                    travelDistance,
                    visitedCountryCount,
                    travelBadgeCount,
                    recentlyReported,
                    totalReportCount);
        } catch (Exception e) {
            log.error("상대방 프로필 조회 중 에러 발생 - userNumber: {}", userNumber, e);
            throw e;
        }
    }

    //  본인 프로필 조회 메서드 (ProfileViewResponse 사용)
    public ProfileViewResponse getProfileView(Integer userNumber) {
        try {
            log.info("내 프로필 조회 시작 - userNumber: {}", userNumber);
            Double travelDistance=0.0;
            Integer visitedCountryCount=0;
            Integer travelBadgeCount=0;
            Users user = userRepository.findUserWithTags(userNumber)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            return new ProfileViewResponse(user, travelDistance, visitedCountryCount, travelBadgeCount);
        } catch (Exception e) {
            log.error("내 프로필 조회 중 에러 발생 - userNumber: {}", userNumber, e);
            throw e;
        }
    }
}
