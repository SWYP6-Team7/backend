package swyp.swyp6_team7.travel.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.member.entity.DeletedUsers;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.TravelTag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Table(name = "travels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_number", updatable = false)
    private int number;

    //작성자 식별자
    @Column(name = "user_number", nullable = false)
    private int userNumber;

    //작성 일시
    @CreatedDate
    @Column(name = "travel_reg_date", nullable = false)
    private LocalDateTime createdAt;

    // 여행지 ID (참조)
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    //여행지
    @Column(name = "travel_location", length = 20)
    private String locationName;

    //여행 시작 일자
    @Column(name = "travel_start_date", nullable = false)
    private LocalDate startDate;

    //여행 종료 일자
    @Column(name = "travel_end_date", nullable = false)
    private LocalDate endDate;

    //제목
    @Column(name = "travel_title", length = 20)
    private String title;

    //상세 설명
    @Lob
    @Column(name = "travel_details", length = 2000)
    private String details;

    //조회수
    @Column(name = "view_count", nullable = false)
    private int viewCount;

    //최대 모집 인원
    @Column(name = "travel_max_person")
    private int maxPerson;

    //모집 성별 카테고리
    @Enumerated(EnumType.STRING)
    @Column(name = "travel_gender", nullable = false, length = 20)
    private GenderType genderType;

    //여행 기간 카테고리
    @Enumerated(EnumType.STRING)
    @Column(name = "travel_period", nullable = false, length = 20)
    private PeriodType periodType;

    //콘텐츠 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "travel_status", nullable = false, length = 20)
    private TravelStatus status;

    @Column(name = "enrollments_last_viewed")
    private LocalDateTime enrollmentsLastViewedAt;

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelTag> travelTags = new ArrayList<>();

    @OneToMany(mappedBy = "travel", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Companion> companions = new ArrayList<>();

    // 기존의 Users 참조 대신 탈퇴 회원을 참조할 수 있는 필드 추가
    @ManyToOne
    @JoinColumn(name = "deleted_number", referencedColumnName = "deletedNumber", nullable = true)
    private DeletedUsers deletedUser;

    @Builder
    public Travel(
            int number, int userNumber, LocalDateTime createdAt, Location location, String locationName,
            LocalDate startDate, LocalDate endDate, String title, String details, int viewCount,
            int maxPerson, GenderType genderType, PeriodType periodType, TravelStatus status,
            LocalDateTime enrollmentsLastViewedAt, List<Tag> tags, DeletedUsers deletedUser
    ) {
        this.number = number;
        this.userNumber = userNumber;
        this.createdAt = createdAt;
        this.location = location;
        this.locationName = locationName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.details = details;
        this.viewCount = viewCount;
        this.maxPerson = maxPerson;
        this.genderType = genderType;
        this.periodType = periodType;
        this.status = status;
        this.enrollmentsLastViewedAt = enrollmentsLastViewedAt;
        this.travelTags = createTravelTags(tags);
        this.deletedUser = deletedUser;
    }

    private List<TravelTag> createTravelTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        return tags.stream()
                .map(tag -> TravelTag.of(this, tag))
                .collect(Collectors.toList());
    }

    public static Travel create(
            int userNumber, Location location, LocalDate startDate, LocalDate endDate,
            String title, String details, int maxPerson, String genderType, String periodType, List<Tag> tags
    ) {
        return Travel.builder()
                .userNumber(userNumber)
                .location(location)
                .locationName(location.getLocationName())
                .startDate(startDate)
                .endDate(endDate)
                .title(title)
                .details(details)
                .viewCount(0)
                .maxPerson(maxPerson)
                .genderType(GenderType.of(genderType))
                .periodType(PeriodType.of(periodType))
                .status(TravelStatus.IN_PROGRESS)
                .tags(tags)
                .build();
    }

    public Travel update(
            Location location, LocalDate startDate, LocalDate endDate,
            String title, String details, int maxPerson, String genderType, String periodType, List<TravelTag> tags
    ) {
        this.location = location;
        this.locationName = location.getLocationName();
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.details = details;
        this.maxPerson = maxPerson;
        this.genderType = GenderType.of(genderType);
        this.periodType = PeriodType.of(periodType);
        this.travelTags = tags;
        return this;
    }

    public void close() {
        this.status = TravelStatus.CLOSED;
    }

    public void delete() {
        this.status = TravelStatus.DELETED;
    }

    public boolean availableForEnroll() {
        if (this.status != TravelStatus.IN_PROGRESS) {
            return false;
        }
        return true;
    }

    public boolean availableForAddCompanion() {
        if (companions.size() >= maxPerson) {
            return false;
        }
        return true;
    }

    public boolean isFullCompanion() {
        if (companions.size() == maxPerson) {
            return true;
        }
        return false;
    }

    public boolean isTravelHostUser(int userNumber) {
        if (this.userNumber != userNumber) {
            return false;
        }
        return true;
    }

    public Long getTravelRange() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    @Override
    public String toString() {
        return "Travel{" +
                "number=" + number +
                ", userNumber=" + userNumber +
                ", createdAt=" + createdAt +
                ", locationName='" + locationName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", title='" + title + '\'' +
                ", details='" + details + '\'' +
                ", viewCount=" + viewCount +
                ", maxPerson=" + maxPerson +
                ", genderType=" + genderType +
                ", periodType=" + periodType +
                ", status=" + status +
                ", enrollmentsLastViewedAt=" + enrollmentsLastViewedAt +
                '}';
    }

}
