package swyp.swyp6_team7.profile.dto.response;

import lombok.Getter;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.tag.domain.UserTagPreference;

import java.time.format.DateTimeFormatter;
import java.util.Set;

@Getter
public class ProfileViewResponse {
    private String email;
    private String name;
    private String gender;
    private String userRegDate;
    private String ageGroup;
    private String[] preferredTags;
    private boolean userSocialTF;

    private Double travelDistance;            // 계산된 여행 거리
    private Integer visitedCountryCount;      // 방문한 국가 개수
    private Integer travelBadgeCount;         // 획득한 여행 뱃지 개수

    public ProfileViewResponse(Users user,
                               Integer visitedCountryCount,
                               Integer travelBadgeCount) {
        this.email = user.getUserEmail();
        this.name = user.getUserName();
        this.gender = user.getUserGender().name();
        this.ageGroup = user.getUserAgeGroup().getValue();
        this.userSocialTF = user.getUserSocialTF();

        this.travelDistance = user.getTotalDistance() != null ? travelDistance : 0.0;
        this.visitedCountryCount = visitedCountryCount != null ? visitedCountryCount : 0;
        this.travelBadgeCount = travelBadgeCount != null ? travelBadgeCount : 0;

        // userRegDate 포매팅
        if (user.getUserRegDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월");
            this.userRegDate = user.getUserRegDate().format(formatter);
        } else {
            this.userRegDate = "";
        }

        // 태그 목록을 가져와서 배열로 변환
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
