package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.plan.dto.request.PlanCreateRequest;
import swyp.swyp6_team7.plan.service.PlanService;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.TravelTagRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.TravelDetailLoginMemberRelatedDto;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@SpringBootTest
class TravelServiceTest {

    @Autowired
    private TravelService travelService;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TravelTagRepository travelTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @MockBean
    private BookmarkRepository bookmarkRepository;

    @MockBean
    private PlanService planService;

    @BeforeTestClass
    void init() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        travelTagRepository.deleteAllInBatch();
        travelRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("create: 여행 콘텐츠를 만들 수 있다.")
    @Test
    public void create() {
        // given
        Location location = locationRepository.save(createLocation("Seoul"));
        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("Seoul")
                .startDate(LocalDate.of(2024, 12, 22))
                .endDate(LocalDate.of(2024, 12, 28))
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType(GenderType.MIXED.toString())
                .periodType(PeriodType.ONE_WEEK.toString())
                .tags(List.of("쇼핑"))
                .build();

        // when
        Travel createdTravel = travelService.create(request, 1);

        // then
        assertThat(travelRepository.findAll()).hasSize(1);
        assertThat(createdTravel.getUserNumber()).isEqualTo(1);
        assertThat(createdTravel.getLocationName()).isEqualTo("Seoul");
        assertThat(createdTravel.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 22));
        assertThat(createdTravel.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 28));
        assertThat(createdTravel.getTitle()).isEqualTo("여행 제목");
        assertThat(createdTravel.getDetails()).isEqualTo("여행 내용");
        assertThat(createdTravel.getViewCount()).isEqualTo(0);
        assertThat(createdTravel.getMaxPerson()).isEqualTo(2);
        assertThat(createdTravel.getGenderType()).isEqualTo(GenderType.MIXED);
        assertThat(createdTravel.getPeriodType()).isEqualTo(PeriodType.ONE_WEEK);
        assertThat(createdTravel.getStatus()).isEqualTo(TravelStatus.IN_PROGRESS);
        assertThat(createdTravel.getEnrollmentsLastViewedAt()).isNull();
        assertThat(createdTravel.getTravelTags()).hasSize(1);
        assertThat(createdTravel.getTravelTags().get(0).getTag().getName()).isEqualTo("쇼핑");
        assertThat(createdTravel.getCompanions()).isEmpty();
        assertThat(createdTravel.getDeletedUser()).isNull();
    }

    @DisplayName("create: 여행 생성 시 여행 기간이 90일을 초과하면 예외가 발생한다.")
    @Test
    void createWithValidateDateRange() {
        // given
        Location location = locationRepository.save(createLocation("Seoul"));
        LocalDate startDate = LocalDate.of(2024, 12, 22);
        LocalDate endDate = startDate.plusDays(90);

        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("Seoul")
                .startDate(startDate)
                .endDate(endDate)
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType(GenderType.MIXED.toString())
                .periodType(PeriodType.ONE_WEEK.toString())
                .tags(List.of("쇼핑"))
                .build();

        // when // then
        assertThatThrownBy(() -> {
            travelService.create(request, 1);
        }).isInstanceOf(MoingApplicationException.class)
                .hasMessage("여행 기간은 90일을 초과할 수 없습니다.");
    }

    @DisplayName("create: 여행 생성 시 여행 일정 생성 메서드를 호출할 수 있다.")
    @Test
    void createWithPlans() {
        // given
        Location location = locationRepository.save(createLocation("Seoul"));

        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("Seoul")
                .startDate(LocalDate.of(2024, 12, 22))
                .endDate(LocalDate.of(2024, 12, 28))
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType(GenderType.MIXED.toString())
                .periodType(PeriodType.ONE_WEEK.toString())
                .tags(List.of("쇼핑"))
                .build();

        given(planService.createPlans(anyInt(), anyList()))
                .willReturn(List.of());

        // when
        Travel createdTravel = travelService.create(request, 1);

        // then
        assertThat(travelRepository.findAll()).hasSize(1);
        then(planService).should(times(1))
                .createPlans(eq(createdTravel.getNumber()), any(List.class));
    }

    @DisplayName("create: 여행 기간보다 여행 일정 개수가 많으면 예외가 발생한다.")
    @Test
    void createWithTooManyPlans() {
        // given
        Location location = locationRepository.save(createLocation("Seoul"));

        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("Seoul")
                .startDate(LocalDate.of(2024, 12, 22))
                .endDate(LocalDate.of(2024, 12, 22))
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType(GenderType.MIXED.toString())
                .periodType(PeriodType.ONE_WEEK.toString())
                .tags(List.of("쇼핑"))
                .plans(List.of(
                        new PlanCreateRequest(1, List.of()),
                        new PlanCreateRequest(2, List.of())
                ))
                .build();

        // when // then
        assertThatThrownBy(() -> {
            travelService.create(request, 1);
        }).isInstanceOf(MoingApplicationException.class)
                .hasMessage("여행 일정 개수는 여행 기간을 초과할 수 없습니다.");
    }

    @DisplayName("getDetailsByNumber: 여행 번호가 한 개 주어졌을 때, 여행 상세 정보를 조회할 수 있다.")
    @Test
    void getDetailsByNumber() {
        // given
        String defaultProfileUrl = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png";
        Users host = userRepository.save(createHostUser());
        Location location = locationRepository.save(createLocation("Seoul"));
        Travel savedTravel = travelRepository.save(createTravel(host.getUserNumber(), location, TravelStatus.IN_PROGRESS));

        // when
        TravelDetailResponse travelDetails = travelService.getDetailsByNumber(savedTravel.getNumber());

        // then
        assertThat(travelDetails.getTravelNumber()).isEqualTo(savedTravel.getNumber());
        assertThat(travelDetails.getUserNumber()).isEqualTo(host.getUserNumber());
        assertThat(travelDetails.getUserAgeGroup()).isEqualTo(AgeGroup.TEEN.getValue());
        assertThat(travelDetails.getUserName()).isEqualTo("주최자 이름");
        assertThat(travelDetails.getProfileUrl()).isEqualTo(defaultProfileUrl);
        assertThat(travelDetails.getLocation()).isEqualTo("Seoul");
        assertThat(travelDetails.getStartDate()).isEqualTo(LocalDate.of(2024, 11, 22));
        assertThat(travelDetails.getEndDate()).isEqualTo(LocalDate.of(2024, 11, 28));
        assertThat(travelDetails.getTitle()).isEqualTo("여행 제목");
        assertThat(travelDetails.getDetails()).isEqualTo("여행 내용");
        assertThat(travelDetails.getViewCount()).isEqualTo(0);      // 조회수
        assertThat(travelDetails.getEnrollCount()).isEqualTo(0);    // 신청 개수
        assertThat(travelDetails.getBookmarkCount()).isEqualTo(0);  // 북마크 개수
        assertThat(travelDetails.getNowPerson()).isEqualTo(0);      // 현재 참가자
        assertThat(travelDetails.getMaxPerson()).isEqualTo(2);
        assertThat(travelDetails.getGenderType()).isEqualTo(GenderType.MIXED.toString());
        assertThat(travelDetails.getPeriodType()).isEqualTo(PeriodType.ONE_WEEK.toString());
        assertThat(travelDetails.getTags()).hasSize(1);
        assertThat(travelDetails.getPostStatus()).isEqualTo(TravelStatus.IN_PROGRESS.toString());
        assertThat(travelDetails.getLoginMemberRelatedInfo()).isNull();
    }

    @DisplayName("getDetailsByNumber: 여행 번호가 주어졌을 때, 여행이 존재하지 않으면 예외가 발생한다.")
    @Test
    void getDetailsByNumberWhenTravelNotExist() {
        // given
        int targetTravelNumber = 10;

        // when // then
        assertThatThrownBy(() -> {
            travelService.getDetailsByNumber(targetTravelNumber);
        }).isInstanceOf(MoingApplicationException.class)
                .hasMessage("해당하는 여행을 찾을 수 없습니다. travelNumber=" + targetTravelNumber);
    }

    @DisplayName("getDetailsByNumber: 여행 번호가 한 개 주어졌을 때, status가 Deleted라면 예외가 발생한다.")
    @Test
    void getDetailsByNumberWhenDeletedStatus() {
        // given
        Users host = userRepository.save(createHostUser());

        Location location = locationRepository.save(createLocation("Seoul"));
        Travel savedTravel = travelRepository.save(createTravel(host.getUserNumber(), location, TravelStatus.DELETED));

        // when // then
        assertThatThrownBy(() -> {
            travelService.getDetailsByNumber(savedTravel.getNumber());
        }).isInstanceOf(MoingApplicationException.class)
                .hasMessage("삭제된 여행 콘텐츠입니다.");
    }

    @DisplayName("getTravelDetailMemberRelatedInfo: 로그인 유저는 여행 상세 조회 요청 시, 자신과 관련된 상세 정보를 가져올 수 있다.")
    @Test
    void getTravelDetailMemberRelatedInfo() {
        // given
        int travelNumber = 10;
        int requestUserNumber = 1;
        int hostUserNumber = 2;
        String status = TravelStatus.IN_PROGRESS.getName();

        Enrollment enrollment = Enrollment.builder()
                .number(5)
                .travelNumber(travelNumber)
                .userNumber(requestUserNumber)
                .status(EnrollmentStatus.PENDING)
                .build();

        given(enrollmentRepository.findPendingEnrollmentByTravelNumberAndUserNumber(anyInt(), anyInt()))
                .willReturn(enrollment.getNumber());
        given(bookmarkRepository.existsByUserNumberAndTravelNumber(requestUserNumber, travelNumber))
                .willReturn(false);

        // when
        TravelDetailLoginMemberRelatedDto memberRelatedInfo = travelService.getTravelDetailMemberRelatedInfo(requestUserNumber, travelNumber, hostUserNumber, status);

        // then
        assertThat(memberRelatedInfo).isNotNull()
                .extracting("hostUser", "enrollmentNumber", "bookmarked")
                .contains(false, 5L, false);
    }

    @DisplayName("getTravelDetailMemberRelatedInfo: 여행 Status가 Draft인 경우 작성자가 아니라면 예외가 발생한다.")
    @Test
    void getTravelDetailMemberRelatedInfoWhenDraft() {
        // given
        int travelNumber = 10;
        int requestUserNumber = 1;
        int hostUserNumber = 2;
        String status = TravelStatus.DRAFT.getName();

        // when // then
        assertThatThrownBy(() -> {
            travelService.getTravelDetailMemberRelatedInfo(requestUserNumber, travelNumber, hostUserNumber, status);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DRAFT 상태의 여행 조회는 작성자만 가능합니다.");
    }

    @DisplayName("update: 여행 콘텐츠를 수정할 수 있다.")
    @Transactional
    @Test
    void update() {
        // given
        int hostUserNumber = 1;
        Location location = locationRepository.save(createLocation("Seoul"));
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, TravelStatus.IN_PROGRESS));

        TravelUpdateRequest request = TravelUpdateRequest.builder()
                .locationName("Seoul")
                .startDate(LocalDate.of(2024, 12, 22))
                .endDate(LocalDate.of(2024, 12, 28))
                .title("여행 제목 수정")
                .details("여행 내용 수정")
                .maxPerson(3)
                .genderType(GenderType.WOMAN_ONLY.toString())
                .periodType(PeriodType.MORE_THAN_MONTH.toString())
                .tags(List.of("쇼핑"))
                .build();

        // when
        Travel updatedTravel = travelService.update(savedTravel.getNumber(), request, 1);

        // then
        assertThat(travelRepository.findAll()).hasSize(1);
        assertThat(updatedTravel.getUserNumber()).isEqualTo(1);
        assertThat(updatedTravel.getLocationName()).isEqualTo("Seoul");
        assertThat(updatedTravel.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 22));
        assertThat(updatedTravel.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 28));
        assertThat(updatedTravel.getTitle()).isEqualTo("여행 제목 수정");
        assertThat(updatedTravel.getDetails()).isEqualTo("여행 내용 수정");
        assertThat(updatedTravel.getMaxPerson()).isEqualTo(3);
        assertThat(updatedTravel.getGenderType()).isEqualTo(GenderType.WOMAN_ONLY);
        assertThat(updatedTravel.getPeriodType()).isEqualTo(PeriodType.MORE_THAN_MONTH);
        assertThat(updatedTravel.getStatus()).isEqualTo(TravelStatus.IN_PROGRESS);
        assertThat(updatedTravel.getTravelTags()).hasSize(1);
        assertThat(updatedTravel.getTravelTags().get(0).getTag().getName()).isEqualTo("쇼핑");

        assertThat(travelTagRepository.findAll()).hasSize(1);
        assertThat(tagRepository.findAll()).hasSize(2)
                .extracting("name")
                .contains("자연", "쇼핑");
    }

    @DisplayName("update: 여행 콘텐츠를 수정할 때, 작성자가 아니라면 예외가 발생한다.")
    @Test
    void updateWhenNotHost() {
        // given
        int hostUserNumber = 1;
        int requestUserNumber = 2;

        Location location = locationRepository.save(createLocation("Seoul"));
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, TravelStatus.IN_PROGRESS));

        TravelUpdateRequest request = TravelUpdateRequest.builder()
                .startDate(LocalDate.of(2024, 11, 22))
                .endDate(LocalDate.of(2024, 11, 28))
                .tags(List.of())
                .build();

        // when //then
        assertThatThrownBy(() -> {
            travelService.update(savedTravel.getNumber(), request, requestUserNumber);
        }).isInstanceOf(MoingApplicationException.class)
                .hasMessage("여행 수정 권한이 없습니다.");
    }

    @DisplayName("delete: 여행 콘텐츠를 삭제할 수 있다.")
    @Test
    void delete() {
        // given
        int hostUserNumber = 1;
        Location location = locationRepository.save(createLocation("Seoul"));
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, TravelStatus.IN_PROGRESS));

        // when
        travelService.delete(savedTravel.getNumber(), hostUserNumber);

        // then
        List<Travel> travels = travelRepository.findAll();
        assertThat(travels).hasSize(1)
                .extracting("number", "status")
                .contains(
                        tuple(savedTravel.getNumber(), TravelStatus.DELETED)
                );
    }

    @DisplayName("delete: 여행 콘텐츠를 삭제할 때, 작성자가 아니라면 예외가 발생한다.")
    @Test
    void deleteWhenNotHost() {
        // given
        int hostUserNumber = 1;
        int requestUserNumber = 2;

        Location location = locationRepository.save(createLocation("Seoul"));
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, TravelStatus.IN_PROGRESS));

        // when //then
        assertThatThrownBy(() -> {
            travelService.delete(savedTravel.getNumber(), requestUserNumber);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 삭제 권한이 없습니다.");
    }

    @DisplayName("getEnrollmentsLastViewedAt: 특정 여행의 enrollmentsLastViewedAt 값을 가져올 수 있다.")
    @Test
    void getEnrollmentsLastViewedAt() {
        // given
        int hostUserNumber = 1;
        Location location = locationRepository.save(createLocation("Seoul"));
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, TravelStatus.IN_PROGRESS));

        // when
        LocalDateTime lastViewedAt = travelService.getEnrollmentsLastViewedAt(savedTravel.getNumber());

        // then
        assertThat(lastViewedAt).isEqualTo(LocalDateTime.of(2024, 11, 17, 12, 0));
    }

    @DisplayName("getEnrollmentsLastViewedAt: 특정 여행의 enrollmentsLastViewedAt 값을 수정할 수 있다.")
    @Test
    void updateEnrollmentLastViewedAt() {
        // given
        int hostUserNumber = 1;
        Location location = locationRepository.save(createLocation("Seoul"));
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, TravelStatus.IN_PROGRESS));

        LocalDateTime updateDateTime = LocalDateTime.of(2024, 11, 20, 10, 0);

        // when
        travelService.updateEnrollmentLastViewedAt(savedTravel.getNumber(), updateDateTime);

        // then
        assertThat(travelRepository.findAll()).hasSize(1)
                .extracting("enrollmentsLastViewedAt")
                .contains(LocalDateTime.of(2024, 11, 20, 10, 0));
    }

    private Location createLocation(String locationName) {
        return Location.builder()
                .locationName(locationName)
                .locationType(LocationType.DOMESTIC)
                .build();
    }

    private Users createHostUser() {
        return Users.builder()
                .userNumber(1)
                .userPw("1234")
                .userEmail("test@mail.com")
                .userName("주최자 이름")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TEEN)
                .userStatus(UserStatus.ABLE)
                .build();
    }

    private Travel createTravel(int hostUserNumber, Location location, TravelStatus status) {
        Tag tag1 = tagRepository.save(Tag.of("자연"));

        return Travel.builder()
                .userNumber(hostUserNumber)
                .location(location)
                .locationName(location.getLocationName())
                .startDate(LocalDate.of(2024, 11, 22))
                .endDate(LocalDate.of(2024, 11, 28))
                .title("여행 제목")
                .details("여행 내용")
                .viewCount(0)
                .maxPerson(2)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(status)
                .tags(List.of(tag1))
                .enrollmentsLastViewedAt(LocalDateTime.of(2024, 11, 17, 12, 0))
                .build();
    }
}
