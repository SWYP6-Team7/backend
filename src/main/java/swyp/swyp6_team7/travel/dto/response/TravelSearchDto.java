package swyp.swyp6_team7.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class TravelSearchDto {

    @NotNull
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    private String postStatus;
    private boolean bookmarked;


    @Builder
    public TravelSearchDto(
            int travelNumber, String title, String location, LocalDate startDate, LocalDate endDate,
            int userNumber, String userName, List<String> tags, int maxPerson, int nowPerson,
            LocalDateTime createdAt, String postStatus, boolean isBookmarked
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
        this.postStatus = postStatus;
        this.bookmarked = isBookmarked;
    }


    @QueryProjection
    public TravelSearchDto(
            Travel travel, int userNumber, String userName,
            int companionCount, List<String> tags
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
        this.postStatus = travel.getStatus().getName();
        this.bookmarked = false;
    }

    public void updateBookmarked(Boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    @Override
    public String toString() {
        return "TravelSearchDto{" +
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
                ", postStatus='" + postStatus + '\'' +
                ", bookmarked=" + bookmarked +
                '}';
    }
}
