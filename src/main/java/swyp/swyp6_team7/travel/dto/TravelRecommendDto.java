package swyp.swyp6_team7.travel.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;
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
public class TravelRecommendDto {

    @NotNull
    private int travelNumber;
    private String title;
    private int userNumber;
    private String userName;
    private String location;
    private List<String> tags;
    private int nowPerson;
    private int maxPerson;
    private LocalDateTime createdAt;
    private LocalDate registerDue;
    private int preferredNumber;
    private boolean bookmarked;

    @Builder
    public TravelRecommendDto(
            int travelNumber, String title, int userNumber, String userName,
            String location, List<String> tags, int nowPerson, int maxPerson,
            LocalDateTime createdAt, LocalDate registerDue, int preferredNumber,
            boolean bookmarked
    ) {
        this.travelNumber = travelNumber;
        this.title = title;
        this.userNumber = userNumber;
        this.userName = userName;
        this.location = location;
        this.tags = tags;
        this.nowPerson = nowPerson;
        this.maxPerson = maxPerson;
        this.createdAt = createdAt;
        this.registerDue = registerDue;
        this.preferredNumber = preferredNumber;
        this.bookmarked = bookmarked;
    }

    @QueryProjection
    public TravelRecommendDto(
            Travel travel, int userNumber, String userName,
            int companionCount, List<String> tags, boolean isBookmarked
    ) {
        this.travelNumber = travel.getNumber();
        this.title = travel.getTitle();
        this.userNumber = userNumber;
        this.userName = userName;
        this.location = travel.getLocation();
        this.tags = tags;
        this.nowPerson = companionCount;
        this.maxPerson = travel.getMaxPerson();
        this.createdAt = travel.getCreatedAt();
        this.registerDue = travel.getDueDate();
        this.bookmarked = isBookmarked;
    }

    public void updatePreferredNumber(Integer number) {
        this.preferredNumber = number;
    }

    @Override
    public String toString() {
        return "TravelRecommendDto{" +
                "travelNumber=" + travelNumber +
                ", title='" + title + '\'' +
                ", userNumber=" + userNumber +
                ", userName='" + userName + '\'' +
                ", location='" + location + '\'' +
                ", tags=" + tags +
                ", nowPerson=" + nowPerson +
                ", maxPerson=" + maxPerson +
                ", createdAt=" + createdAt +
                ", registerDue=" + registerDue +
                ", preferredNumber=" + preferredNumber +
                ", bookmarked=" + bookmarked +
                '}';
    }
}

