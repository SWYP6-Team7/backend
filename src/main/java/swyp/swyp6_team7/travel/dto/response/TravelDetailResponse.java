package swyp.swyp6_team7.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.travel.domain.Travel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TravelDetailResponse {

    private int travelNumber;
    private String title;
    private String summary;
    private int userNumber;
    private String userName;
    private List<String> tags;
    private String details;
    private int viewCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate travelStartAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate travelEndAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerDue;
    private String location;
    private int minPerson;
    private int maxPerson;
    private int budget;
    private String postStatus;
    //TODO: Image 처리, 현재 모집 확정된 인원수 처리


    @Builder
    public TravelDetailResponse(
            int travelNumber, String title, String summary,
            int userNumber, String userName, List<String> tags, String details, int viewCount,
            LocalDateTime createdAt, LocalDate travelStartAt, LocalDate travelEndAt,
            LocalDateTime registerDue, String location, int minPerson, int maxPerson,
            int budget, String postStatus
    ) {
        this.travelNumber = travelNumber;
        this.title = title;
        this.summary = summary;
        this.userNumber = userNumber;
        this.userName = userName;
        this.tags = tags;
        this.details = details;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.travelStartAt = travelStartAt;
        this.travelEndAt = travelEndAt;
        this.registerDue = registerDue;
        this.location = location;
        this.minPerson = minPerson;
        this.maxPerson = maxPerson;
        this.budget = budget;
        this.postStatus = postStatus;
    }

    @QueryProjection
    public TravelDetailResponse(
            Travel travel, int userNumber, String userName,
            List<String> tags
    ) {
        this.travelNumber = travel.getNumber();
        this.title = travel.getTitle();
        this.summary = travel.getSummary();
        this.userNumber = userNumber;
        this.userName = userName;
        this.tags = tags;
        this.details = travel.getDetails();
        this.viewCount = travel.getViewCount();
        this.createdAt = travel.getCreatedAt();
        this.travelStartAt = travel.getStartAt();
        this.travelEndAt = travel.getEndAt();
        this.registerDue = travel.getDueDateTime();
        this.location = travel.getLocation();
        this.minPerson = travel.getMinPerson();
        this.maxPerson = travel.getMaxPerson();
        this.budget = travel.getBudget();
        this.postStatus = travel.getStatus().getName();
    }

    public static TravelDetailResponse from(
            Travel travel,
            List<String> tags,
            int userNumber, String userName
    ) {
        return TravelDetailResponse.builder()
                .travelNumber(travel.getNumber())
                .title(travel.getTitle())
                .summary(travel.getSummary())
                .userNumber(userNumber)
                .userName(userName)
                .tags(tags)
                .details(travel.getDetails())
                .viewCount(travel.getViewCount())
                .createdAt(travel.getCreatedAt())
                .travelStartAt(travel.getStartAt())
                .travelEndAt(travel.getEndAt())
                .registerDue(travel.getDueDateTime())
                .location(travel.getLocation())
                .minPerson(travel.getMinPerson())
                .maxPerson(travel.getMaxPerson())
                .budget(travel.getBudget())
                .postStatus(travel.getStatus().getName())
                .build();
    }

    @Override
    public String toString() {
        return "TravelDetailResponse{" +
                "travelNumber=" + travelNumber +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", userNumber=" + userNumber +
                ", userName='" + userName + '\'' +
                ", tags=" + tags +
                ", details='" + details + '\'' +
                ", viewCount=" + viewCount +
                ", createdAt=" + createdAt +
                ", travelStartAt=" + travelStartAt +
                ", travelEndAt=" + travelEndAt +
                ", registerDue=" + registerDue +
                ", location='" + location + '\'' +
                ", minPerson=" + minPerson +
                ", maxPerson=" + maxPerson +
                ", budget=" + budget +
                ", postStatus='" + postStatus + '\'' +
                '}';
    }
}