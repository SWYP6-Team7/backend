package swyp.swyp6_team7.profile.dto;

import lombok.Getter;
import lombok.Setter;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.tag.domain.UserTagPreference;

import java.time.format.DateTimeFormatter;
import java.util.Set;

@Getter
public class OtherUserProfileResponse {
    // 기본 정보
    private String name;
    private String userRegDate; // 예: "2025년 2월"
    private String ageGroup;
    private String[] preferredTags;
    // 프로필 이미지

    // 추가 정보
    private Double travelDistance; // 계산된 여행 거리
    private Integer visitedCountryCount; // 방문한 국가 수
    private Integer travelBadgeCount; // 획득한 여행 뱃지 수

    private Integer createdTravelCount; // 사용자가 만든 여행 개수
    private Integer participatedTravelCount; // 사용자가 참가한 여행 개수

    private Boolean recentlyReported; // 최근 신고 여부 계속 보이는 건가?
    private Integer totalReportCount; // 누적 신고 횟수

    public OtherUserProfileResponse(Users user,
                                    Double travelDistance,
                                    Integer visitedCountryCount,
                                    Integer travelBadgeCount,
                                    Integer createdTravelCount,
                                    Integer participatedTravelCount,
                                    Boolean recentlyReported,
                                    Integer totalReportCount) {
        this.name = user.getUserName();
        this.ageGroup = user.getUserAgeGroup().getValue();

        this.travelDistance = travelDistance != null ? travelDistance : 0.0;
        this.visitedCountryCount = visitedCountryCount != null ? visitedCountryCount : 0;
        this.travelBadgeCount = travelBadgeCount != null ? travelBadgeCount : 0;
        this.createdTravelCount = createdTravelCount != null ? createdTravelCount : 0;
        this.participatedTravelCount = participatedTravelCount != null ? participatedTravelCount : 0;
        this.recentlyReported = recentlyReported != null ? recentlyReported : false;
        this.totalReportCount = totalReportCount != null ? totalReportCount : 0;

        // 가입일 포매팅
        if (user.getUserRegDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월");
            this.userRegDate = user.getUserRegDate().format(formatter);
        } else {
            this.userRegDate = "";
        }

        // 선호 태그 변환
        Set<UserTagPreference> tagPreferences = user.getTagPreferences();
        if (tagPreferences != null && !tagPreferences.isEmpty()) {
            this.preferredTags = tagPreferences.stream()
                    .map(preference -> preference.getTag().getName())
                    .toArray(String[]::new);
        } else {
            this.preferredTags = new String[0]; // 태그가 없을 경우 빈 배열
        }
    }

}
