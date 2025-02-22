package swyp.swyp6_team7.travel.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.travel.dto.TravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelDetailLoginMemberRelatedDto;

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
    private String periodType;
    private List<String> tags;
    private String postStatus;
    private TravelDetailLoginMemberRelatedDto loginMemberRelatedInfo; // 로그인 사용자 관련 추가 정보


    @Builder
    public TravelDetailResponse(
            int travelNumber, int userNumber, String userName, String userAgeGroup, String profileUrl, LocalDateTime createdAt,
            String location, String title, String details, int viewCount, int enrollCount, int bookmarkCount,
            int nowPerson, int maxPerson, String genderType, String periodType, List<String> tags, String postStatus
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
        this.periodType = periodType;
        this.tags = tags;
        this.postStatus = postStatus;
    }

    public TravelDetailResponse(
            TravelDetailDto travelDetail, String hostProfileImageUrl, int enrollCount, int bookmarkCount
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
        this.periodType = travelDetail.getTravel().getPeriodType().toString();
        this.tags = travelDetail.getTags();
        this.postStatus = travelDetail.getTravel().getStatus().toString();
        this.loginMemberRelatedInfo = null;
    }

    public void updateLoginMemberRelatedInfo(TravelDetailLoginMemberRelatedDto memberRelatedDto) {
        this.loginMemberRelatedInfo = memberRelatedDto;
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
                ", periodType='" + periodType + '\'' +
                ", tags=" + tags +
                ", postStatus='" + postStatus + '\'' +
                ", loginMemberRelatedInfo=" + loginMemberRelatedInfo +
                '}';
    }
}
