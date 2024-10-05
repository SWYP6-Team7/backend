package swyp.swyp6_team7.travel.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.travel.domain.Travel;

import java.util.List;

@Getter
public class TravelDetailDto {

    private Travel travel;
    private int hostNumber;
    private String hostName;
    private String hostAgeGroup;
    private int companionCount;
    private List<String> tags;
    private boolean bookmarked;

    @QueryProjection
    public TravelDetailDto(
            Travel travel, int hostNumber, String hostName, AgeGroup hostAgeGroup,
            int companionCount, List<String> tags, boolean isBookmarked
    ) {
        this.travel = travel;
        this.hostNumber = hostNumber;
        this.hostName = hostName;
        this.hostAgeGroup = hostAgeGroup.getValue();
        this.companionCount = companionCount;
        this.tags = tags;
        this.bookmarked = isBookmarked;
    }

}
