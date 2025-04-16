package swyp.swyp6_team7.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.profile.dto.TargetUserProfileResponse;
import swyp.swyp6_team7.profile.dto.VisitedCountryStats;
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

    // 상대방 프로필 조회 메서드 (TargetUserProfileResponse 사용)
    public TargetUserProfileResponse getOtherUserProfile(Integer userNumber) {
        try {
            log.info("상대방 프로필 조회 시작 - userNumber: {}", userNumber);
            Users user = userRepository.findUserWithTags(userNumber)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Integer createdTravelCount = travelListService.countCreatedTravelsByUser(userNumber);      // 사용자가 만든 여행 개수
            Integer participatedTravelCount = travelAppliedService.countAppliedTrpsByUser(userNumber); // 사용자가 참가한 여행 개수

            VisitedCountryStats stats = visitedCountryLogService.calculateVisitedCountryStats(userNumber);
            Double travelDistance = stats.getTravelDistance(); // 계산된 여행 거리(Km)
            Integer visitedCountryCount = stats.getVisitedCountryCount(); // 방문한 국가 수

            // 추가 정보는 아직 구현되지 않았으므로 기본값을 사용합니다.
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
}
