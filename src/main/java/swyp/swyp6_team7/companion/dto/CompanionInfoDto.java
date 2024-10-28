package swyp.swyp6_team7.companion.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import swyp.swyp6_team7.member.entity.AgeGroup;

@Getter
public class CompanionInfoDto {

    private Integer userNumber;
    private String userName;
    private String ageGroup;
    private String profileUrl;

    @QueryProjection
    public CompanionInfoDto(Integer userNumber, String userName, AgeGroup ageGroup, String profileUrl) {
        this.userNumber = userNumber;
        this.userName = userName;
        this.ageGroup = ageGroup.getValue();
        this.profileUrl = profileUrl;
    }

    @Override
    public String toString() {
        return "CompanionInfoDto{" +
                "userNumber=" + userNumber +
                ", userName='" + userName + '\'' +
                ", ageGroup='" + ageGroup + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                '}';
    }
}
