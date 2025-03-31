package swyp.swyp6_team7.auth.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserRole;
import swyp.swyp6_team7.member.entity.UserStatus;

@Getter
@Setter
//@AllArgsConstructor
public class UserInfoDto {
    private Integer userNumber;
    private String userName;
    private String userEmail;
    private UserStatus userStatus;
    @Nullable
    private String socialLoginId;
    private Gender gender;
    private AgeGroup ageGroup;
    private String provider;
    private UserRole userRole;

    public UserInfoDto(
            Integer userNumber,
            String userName,
            String userEmail,
            UserStatus userStatus,
            String socialLoginId,
            UserRole userRole
    ) {
        this.userNumber = userNumber;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userStatus = userStatus;
        this.socialLoginId = socialLoginId;
        this.userRole = userRole;
    }

    public UserInfoDto(
            Integer userNumber,
            String userName,
            String userEmail,
            UserStatus userStatus,
            String socialLoginId,
            Gender gender,
            AgeGroup ageGroup,
            String provider,
            UserRole userRole
    ) {
        this.userNumber = userNumber;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userStatus = userStatus;
        this.socialLoginId = socialLoginId;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.provider = provider;
        this.userRole = userRole;
    }

    @Override
    public String toString() {
        return "UserInfo-userNumber:" + userNumber + ", userName:" + userName + ", userEmail:" + userEmail + ", userStatus:" + userStatus;
    }
}
