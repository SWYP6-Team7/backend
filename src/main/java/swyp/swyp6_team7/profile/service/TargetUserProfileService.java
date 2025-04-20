package swyp.swyp6_team7.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberBlockService;
import swyp.swyp6_team7.profile.dto.UserReportStats;
import swyp.swyp6_team7.profile.dto.response.TargetUserProfileResponse;
import swyp.swyp6_team7.travel.service.TravelAppliedService;
import swyp.swyp6_team7.travel.service.TravelListService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TargetUserProfileService {
    private final UserRepository userRepository;
    private final TravelAppliedService travelAppliedService;
    private final TravelListService travelListService;
    private final VisitedCountryLogService visitedCountryLogService;
    private final ImageRepository imageRepository;
    private final MemberBlockService memberBlockService;

    // TODO: 링크 수정
    private final static String DEFAULT_PROFILE_IMAGE_URL = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png";

    // 상대방 프로필 조회 메서드 (TargetUserProfileResponse 사용)
    public TargetUserProfileResponse getOtherUserProfile(Integer userNumber) {
        try {
            log.info("상대방 프로필 조회 시작 - userNumber: {}", userNumber);
            Users user = userRepository.findUserWithTags(userNumber)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            String profileImageUrl = imageRepository.findUrlByRelatedUserNumber(userNumber)
                    .orElse(DEFAULT_PROFILE_IMAGE_URL);;

            Integer createdTravelCount = travelListService.countCreatedTravelsByUser(userNumber);      // 사용자가 만든 여행 개수
            Integer participatedTravelCount = travelAppliedService.countAppliedTrpsByUser(userNumber); // 사용자가 참가한 여행 개수

            Integer visitedCountryCount = visitedCountryLogService.calculateVisitedCountryCount(userNumber); // 방문한 국가 수

            // TODO
            Integer travelBadgeCount = 0; // 획득한 여행 뱃지 수

            UserReportStats reportStats = memberBlockService.getUserReportStats(userNumber);
            Boolean recentlyReported = reportStats.isRecentlyReported(); //최근 신고 여부
            Integer totalReportCount = reportStats.getTotalReportCount(); // 누적 신고 수
            Integer recentReportCount = reportStats.getRecentReportCount(); // 최근 신고 수


            return new TargetUserProfileResponse(
                    user,
                    profileImageUrl,
                    createdTravelCount,
                    participatedTravelCount,
                    visitedCountryCount,
                    travelBadgeCount,
                    recentlyReported,
                    totalReportCount,
                    recentReportCount);
        } catch (Exception e) {
            log.error("상대방 프로필 조회 중 에러 발생 - userNumber: {}", userNumber, e);
            throw e;
        }
    }
}
