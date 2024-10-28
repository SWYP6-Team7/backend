package swyp.swyp6_team7.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.travel.dto.TravelDetailDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TravelDetailResponse {

    private int travelNumber;
    private int userNumber;     //주최자 번호
    private String userName;    //주최자 이름
    private String userAgeGroup; //주최자 연령대
    private String profileUrl;  //주최자 프로필 이미지 url
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    private String location;
    private String title;
    private String details;
    private int viewCount;      //조회수
    private int enrollCount;    //신청수
    private int bookmarkCount;  //관심수(북마크수)
    private int nowPerson;      //현재 모집 인원
    private int maxPerson;      //최대 모집 인원
    private String genderType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private String periodType;
    private List<String> tags;
    private String postStatus;
    private boolean hostUserCheck;       //주최자 여부
    private Long enrollmentNumber;       //신청 번호(없으면 null)
    private boolean bookmarked;     //북마크 여부

    @Builder
    public TravelDetailResponse(
            int travelNumber, int userNumber, String userName, String userAgeGroup, String profileUrl, LocalDateTime createdAt,
            String location, String title, String details, int viewCount, int enrollCount, int bookmarkCount,
            int nowPerson, int maxPerson, String genderType, LocalDate dueDate, String periodType,
            List<String> tags, String postStatus, boolean hostUserCheck, Long enrollmentNumber,
            boolean isBookmarked
    ) {
        this.travelNumber = travelNumber;
        this.userNumber = userNumber;
        this.userName = userName;
        this.userAgeGroup = userAgeGroup;
        this.profileUrl = profileUrl;
        this.createdAt = createdAt;
        this.location = location;
        this.title = title;
        this.details = details;
        this.viewCount = viewCount;
        this.enrollCount = enrollCount;
        this.bookmarkCount = bookmarkCount;
        this.nowPerson = nowPerson;
        this.maxPerson = maxPerson;
        this.genderType = genderType;
        this.dueDate = dueDate;
        this.periodType = periodType;
        this.tags = tags;
        this.postStatus = postStatus;
        this.hostUserCheck = hostUserCheck;
        this.enrollmentNumber = enrollmentNumber;
        this.bookmarked = isBookmarked;
    }

    public TravelDetailResponse(
            TravelDetailDto travelDetail, String hostProfileImageUrl,
            int enrollCount, int bookmarkCount
    ) {
        this.travelNumber = travelDetail.getTravel().getNumber();
        this.userNumber = travelDetail.getHostNumber();
        this.userName = travelDetail.getHostName();
        this.userAgeGroup = travelDetail.getHostAgeGroup();
        this.profileUrl = hostProfileImageUrl;
        this.createdAt = travelDetail.getTravel().getCreatedAt();
        this.location = travelDetail.getTravel().getLocationName();
        this.title = travelDetail.getTravel().getTitle();
        this.details = travelDetail.getTravel().getDetails();
        this.viewCount = travelDetail.getTravel().getViewCount();
        this.enrollCount = enrollCount;
        this.bookmarkCount = bookmarkCount;
        this.nowPerson = travelDetail.getCompanionCount();
        this.maxPerson = travelDetail.getTravel().getMaxPerson();
        this.genderType = travelDetail.getTravel().getGenderType().toString();
        this.dueDate = travelDetail.getTravel().getDueDate();
        this.periodType = travelDetail.getTravel().getPeriodType().toString();
        this.tags = travelDetail.getTags();
        this.postStatus = travelDetail.getTravel().getStatus().toString();
        this.bookmarked = travelDetail.isBookmarked();
    }


    public void setHostUserCheckTrue() {
        this.hostUserCheck = true;
    }

    public void setEnrollmentNumber(Enrollment enrollment) {
        if (enrollment==null) {
            this.enrollmentNumber = null;
        } else {
            this.enrollmentNumber = enrollment.getNumber();
        }
    }

    @Override
    public String toString() {
        return "TravelDetailResponse{" +
                "travelNumber=" + travelNumber +
                ", userNumber=" + userNumber +
                ", userName='" + userName + '\'' +
                ", userAgeGroup='" + userAgeGroup + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                ", createdAt=" + createdAt +
                ", location='" + location + '\'' +
                ", title='" + title + '\'' +
                ", details='" + details + '\'' +
                ", viewCount=" + viewCount +
                ", enrollCount=" + enrollCount +
                ", bookmarkCount=" + bookmarkCount +
                ", nowPerson=" + nowPerson +
                ", maxPerson=" + maxPerson +
                ", genderType='" + genderType + '\'' +
                ", dueDate=" + dueDate +
                ", periodType='" + periodType + '\'' +
                ", tags=" + tags +
                ", postStatus='" + postStatus + '\'' +
                ", hostUserCheck=" + hostUserCheck +
                ", enrollmentNumber=" + enrollmentNumber +
                ", bookmarked=" + bookmarked +
                '}';
    }
}