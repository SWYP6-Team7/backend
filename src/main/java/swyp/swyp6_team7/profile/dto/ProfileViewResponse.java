package swyp.swyp6_team7.profile.dto;

import lombok.Getter;
import lombok.Setter;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.tag.domain.UserTagPreference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Getter
@Setter
public class ProfileViewResponse {
    private String email;
    private String name;
    private String gender;
    private String userRegDate;
    private String ageGroup;
    private String[] preferredTags;
    private boolean userSocialTF;

    public ProfileViewResponse(Users user) {
        this.email = user.getUserEmail();
        this.name = user.getUserName();
        this.gender = user.getUserGender().name();
        this.ageGroup = user.getUserAgeGroup().getValue();
        this.userSocialTF = user.getUserSocialTF();

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
