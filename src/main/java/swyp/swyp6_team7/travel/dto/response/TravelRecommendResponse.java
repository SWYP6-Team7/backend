package swyp.swyp6_team7.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.travel.dto.TravelRecommendForMemberDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendForNonMemberDto;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TravelRecommendResponse {

    @NotNull
    private int travelNumber;
    private String title;
    private String location;
    private int userNumber;
    private String userName;
    private List<String> tags;
    private int nowPerson;
    private int maxPerson;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    private boolean bookmarked;

    public TravelRecommendResponse(TravelRecommendForMemberDto dto) {
        this.travelNumber = dto.getTravelNumber();
        this.title = dto.getTitle();
        this.location = dto.getLocation();
        this.userNumber = dto.getUserNumber();
        this.userName = dto.getUserName();
        this.tags = dto.getTags();
        this.nowPerson = dto.getNowPerson();
        this.maxPerson = dto.getMaxPerson();
        this.createdAt = dto.getCreatedAt();
        this.bookmarked = dto.isBookmarked();
    }

    public TravelRecommendResponse(TravelRecommendForNonMemberDto dto) {
        this.travelNumber = dto.getTravelNumber();
        this.title = dto.getTitle();
        this.location = dto.getLocation();
        this.userNumber = dto.getUserNumber();
        this.userName = dto.getUserName();
        this.tags = dto.getTags();
        this.nowPerson = dto.getNowPerson();
        this.maxPerson = dto.getMaxPerson();
        this.createdAt = dto.getCreatedAt();
        this.bookmarked = false;
    }

}
