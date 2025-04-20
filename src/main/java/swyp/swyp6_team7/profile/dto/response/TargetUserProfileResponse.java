package swyp.swyp6_team7.profile.dto.response;

import lombok.Getter;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.tag.domain.UserTagPreference;

import java.time.format.DateTimeFormatter;
import java.util.Set;

@Getter
public class TargetUserProfileResponse {
    // 기본 정보
    private String name;
    private String userRegDate; // 예: "2025년 2월"
    private String ageGroup;
    private String[] preferredTags;
    // 프로필 이미지
    private String profileImageUrl;

    // 추가 정보
    private Integer createdTravelCount; // 생성한 여행 수
    private Integer participatedTravelCount; // 참가한 여행 수
    private Double travelDistance; // 계산된 여행 거리
    private Integer visitedCountryCount; // 방문한 국가 수
    //TODO
    private Integer travelBadgeCount; // 획득한 여행 뱃지 수

    private Boolean recentlyReported; // 최근 신고 여부
    private Integer totalReportCount; // 누적 신고 횟수
    private Integer recentReportCount; // 최근 신고 횟수

    public TargetUserProfileResponse(Users user,
                                     String profileImageUrl,
                                     Integer createdTravelCount,
                                     Integer participatedTravelCount,
                                     Integer visitedCountryCount,
                                     Integer travelBadgeCount,
                                     Boolean recentlyReported,
                                     Integer totalReportCount,
                                     Integer recentReportCount) {
        this.name = user.getUserName();
        this.ageGroup = user.getUserAgeGroup().getValue();
        this.profileImageUrl = profileImageUrl;
        this.createdTravelCount = createdTravelCount != null ? createdTravelCount : 0;
        this.participatedTravelCount = participatedTravelCount != null ? participatedTravelCount : 0;
        this.travelDistance = user.getTotalDistance();
        this.visitedCountryCount = visitedCountryCount != null ? visitedCountryCount : 0;
        this.travelBadgeCount = travelBadgeCount != null ? travelBadgeCount : 0;
        this.recentlyReported = recentlyReported != null ? recentlyReported : false;
        this.totalReportCount = totalReportCount != null ? totalReportCount : 0;
        this.recentReportCount = recentReportCount != null ? recentReportCount : 0;

        if (user.getUserRegDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월");
            this.userRegDate = user.getUserRegDate().format(formatter);
        } else {
            this.userRegDate = "";
        }

        Set<UserTagPreference> tagPreferences = user.getTagPreferences();
        if (tagPreferences != null && !tagPreferences.isEmpty()) {
            this.preferredTags = tagPreferences.stream()
                    .map(preference -> preference.getTag().getName())
                    .toArray(String[]::new);
        } else {
            this.preferredTags = new String[0];
        }
    }


}
