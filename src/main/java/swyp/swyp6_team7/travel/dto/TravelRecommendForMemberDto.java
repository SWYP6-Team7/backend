package swyp.swyp6_team7.travel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.travel.domain.Travel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TravelRecommendForMemberDto {

    private int travelNumber;
    private String title;
    private String location;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private int userNumber;
    private String userName;
    private List<String> tags;
    private int nowPerson;
    private int maxPerson;
    private LocalDateTime createdAt;
    private int preferredNumber;
    private boolean bookmarked;

    @Builder
    public TravelRecommendForMemberDto(
            int travelNumber, String title, String location, LocalDate startDate, LocalDate endDate,
            int userNumber, String userName, List<String> tags, int nowPerson, int maxPerson,
            LocalDateTime createdAt, int preferredNumber, boolean isBookmarked
    ) {
        this.travelNumber = travelNumber;
        this.title = title;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userNumber = userNumber;
        this.userName = userName;
        this.tags = tags;
        this.nowPerson = nowPerson;
        this.maxPerson = maxPerson;
        this.createdAt = createdAt;
        this.preferredNumber = preferredNumber;
        this.bookmarked = isBookmarked;
    }

    @QueryProjection
    public TravelRecommendForMemberDto(
            Travel travel, int userNumber, String userName,
            int companionCount, List<String> tags, boolean isBookmarked
    ) {
        this.travelNumber = travel.getNumber();
        this.title = travel.getTitle();
        this.location = travel.getLocationName();
        this.startDate = travel.getStartDate();
        this.endDate = travel.getEndDate();
        this.userNumber = userNumber;
        this.userName = userName;
        this.tags = tags;
        this.nowPerson = companionCount;
        this.maxPerson = travel.getMaxPerson();
        this.createdAt = travel.getCreatedAt();
        this.bookmarked = isBookmarked;
    }

    public void updatePreferredNumber(Integer number) {
        this.preferredNumber = number;
    }

    @Override
    public String toString() {
        return "TravelRecommendForMemberDto{" +
                "travelNumber=" + travelNumber +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userNumber=" + userNumber +
                ", userName='" + userName + '\'' +
                ", tags=" + tags +
                ", nowPerson=" + nowPerson +
                ", maxPerson=" + maxPerson +
                ", createdAt=" + createdAt +
                ", preferredNumber=" + preferredNumber +
                ", bookmarked=" + bookmarked +
                '}';
    }
}

