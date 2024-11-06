package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.TravelTagRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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

    @DisplayName("create: 여행 콘텐츠를 만들 수 있다")
    @Test
    public void create() {
        // given
        Location location = locationRepository.save(createLocation("Seoul"));
        LocalDate dueDate = LocalDate.of(2024, 11, 4);

        TravelCreateRequest request = TravelCreateRequest.builder()
                .locationName("Seoul")
                .title("여행 제목")
                .details("여행 내용")
                .maxPerson(2)
                .genderType(GenderType.MIXED.toString())
                .dueDate(dueDate)
                .periodType(PeriodType.ONE_WEEK.toString())
                .tags(List.of())
                .completionStatus(true)
                .build();

        // when
        Travel createdTravel = travelService.create(request, 1);

        // then
        assertThat(travelRepository.findAll()).hasSize(1);
        assertThat(createdTravel.getUserNumber()).isEqualTo(1);
        assertThat(createdTravel.getLocationName()).isEqualTo("Seoul");
        assertThat(createdTravel.getTitle()).isEqualTo("여행 제목");
        assertThat(createdTravel.getDetails()).isEqualTo("여행 내용");
        assertThat(createdTravel.getViewCount()).isEqualTo(0);
        assertThat(createdTravel.getMaxPerson()).isEqualTo(2);
        assertThat(createdTravel.getGenderType()).isEqualTo(GenderType.MIXED);
        assertThat(createdTravel.getDueDate()).isEqualTo(dueDate);
        assertThat(createdTravel.getPeriodType()).isEqualTo(PeriodType.ONE_WEEK);
        assertThat(createdTravel.getStatus()).isEqualTo(TravelStatus.IN_PROGRESS);
        assertThat(createdTravel.getEnrollmentsLastViewedAt()).isNull();
        assertThat(createdTravel.getTravelTags()).isEmpty();
        assertThat(createdTravel.getCompanions()).isEmpty();
        assertThat(createdTravel.getDeletedUser()).isNull();
    }

    @DisplayName("getDetailsByNumber: 여행 번호가 한 개 주어졌을 때, 여행 상세 정보를 조회할 수 있다.")
    @Test
    void getDetailsByNumber() {
        // given
        String defaultProfileUrl = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png";
        Users host = userRepository.save(createHostUser());

        Location location = locationRepository.save(createLocation("Seoul"));
        LocalDate dueDate = LocalDate.of(2024, 11, 4);
        Travel savedTravel = travelRepository.save(createTravel(host.getUserNumber(), location, dueDate, TravelStatus.IN_PROGRESS));

        // when
        TravelDetailResponse travelDetails = travelService.getDetailsByNumber(savedTravel.getNumber(), 2);

        // then
        assertThat(travelDetails.getTravelNumber()).isEqualTo(savedTravel.getNumber());
        assertThat(travelDetails.getUserNumber()).isEqualTo(host.getUserNumber());
        assertThat(travelDetails.getUserAgeGroup()).isEqualTo(AgeGroup.TEEN.getValue());
        assertThat(travelDetails.getUserName()).isEqualTo("주최자 이름");
        assertThat(travelDetails.getProfileUrl()).isEqualTo(defaultProfileUrl);
        assertThat(travelDetails.getLocation()).isEqualTo("Seoul");
        assertThat(travelDetails.getTitle()).isEqualTo("여행 제목");
        assertThat(travelDetails.getDetails()).isEqualTo("여행 내용");
        assertThat(travelDetails.getViewCount()).isEqualTo(0);      // 조회수
        assertThat(travelDetails.getEnrollCount()).isEqualTo(0);    // 신청 개수
        assertThat(travelDetails.getBookmarkCount()).isEqualTo(0);  // 북마크 개수
        assertThat(travelDetails.getNowPerson()).isEqualTo(0);      // 현재 참가자
        assertThat(travelDetails.getMaxPerson()).isEqualTo(2);
        assertThat(travelDetails.getGenderType()).isEqualTo(GenderType.MIXED.toString());
        assertThat(travelDetails.getDueDate()).isEqualTo(dueDate);
        assertThat(travelDetails.getPeriodType()).isEqualTo(PeriodType.ONE_WEEK.toString());
        assertThat(travelDetails.getTags()).isEmpty();
        assertThat(travelDetails.getPostStatus()).isEqualTo(TravelStatus.IN_PROGRESS.toString());
        assertThat(travelDetails.isHostUserCheck()).isFalse();
        assertThat(travelDetails.getEnrollmentNumber()).isNull();
        assertThat(travelDetails.isBookmarked()).isFalse();
    }

    @DisplayName("getDetailsByNumber: 여행 번호가 한 개 주어졌을 때, status가 Deleted인 경우 예외가 발생한다.")
    @Test
    void getDetailsByNumberWhenDeletedStatus() {
        // given
        Users host = userRepository.save(createHostUser());

        Location location = locationRepository.save(createLocation("Seoul"));
        LocalDate dueDate = LocalDate.of(2024, 11, 4);
        Travel savedTravel = travelRepository.save(createTravel(host.getUserNumber(), location, dueDate, TravelStatus.DELETED));

        // when // then
        assertThatThrownBy(() -> {
            travelService.getDetailsByNumber(savedTravel.getNumber(), 2);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deleted 상태의 여행 콘텐츠입니다.");
    }

    @DisplayName("getDetailsByNumber: 여행 번호가 한 개 주어졌을 때, status가 Draft인 경우 작성자가 아니라면 예외가 발생한다.")
    @Test
    void getDetailsByNumberWhenDraftStatus() {
        // given
        Users host = userRepository.save(createHostUser());

        Location location = locationRepository.save(createLocation("Seoul"));
        LocalDate dueDate = LocalDate.of(2024, 11, 4);
        Travel savedTravel = travelRepository.save(createTravel(host.getUserNumber(), location, dueDate, TravelStatus.DRAFT));

        // when // then
        assertThatThrownBy(() -> {
            travelService.getDetailsByNumber(savedTravel.getNumber(), 2);
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
        LocalDate dueDate = LocalDate.of(2024, 11, 4);
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, dueDate, TravelStatus.IN_PROGRESS));

        TravelUpdateRequest request = TravelUpdateRequest.builder()
                .locationName("Seoul")
                .title("여행 제목 수정")
                .details("여행 내용 수정")
                .maxPerson(3)
                .genderType(GenderType.WOMAN_ONLY.toString())
                .dueDate(LocalDate.of(2024, 11, 5))
                .periodType(PeriodType.MORE_THAN_MONTH.toString())
                .tags(List.of())
                .completionStatus(true)
                .build();

        // when
        Travel updatedTravel = travelService.update(savedTravel.getNumber(), request, 1);

        // then
        assertThat(travelRepository.findAll()).hasSize(1);
        assertThat(updatedTravel.getUserNumber()).isEqualTo(1);
        assertThat(updatedTravel.getLocationName()).isEqualTo("Seoul");
        assertThat(updatedTravel.getTitle()).isEqualTo("여행 제목 수정");
        assertThat(updatedTravel.getDetails()).isEqualTo("여행 내용 수정");
        assertThat(updatedTravel.getMaxPerson()).isEqualTo(3);
        assertThat(updatedTravel.getGenderType()).isEqualTo(GenderType.WOMAN_ONLY);
        assertThat(updatedTravel.getDueDate()).isEqualTo(LocalDate.of(2024, 11, 5));
        assertThat(updatedTravel.getPeriodType()).isEqualTo(PeriodType.MORE_THAN_MONTH);
        assertThat(updatedTravel.getStatus()).isEqualTo(TravelStatus.IN_PROGRESS);
        assertThat(updatedTravel.getTravelTags()).isEmpty();
    }

    @DisplayName("update: 여행 콘텐츠를 수정할 때, 작성자가 아니라면 예외가 발생한다.")
    @Test
    void updateWhenNotHost() {
        // given
        int hostUserNumber = 1;
        int requestUserNumber = 2;

        Location location = locationRepository.save(createLocation("Seoul"));
        LocalDate dueDate = LocalDate.of(2024, 11, 4);
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, dueDate, TravelStatus.IN_PROGRESS));

        TravelUpdateRequest request = TravelUpdateRequest.builder()
                .tags(List.of())
                .completionStatus(true)
                .build();

        // when //then
        assertThatThrownBy(() -> {
            travelService.update(savedTravel.getNumber(), request, requestUserNumber);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 수정 권한이 없습니다.");
    }

    @DisplayName("delete: 여행 콘텐츠를 삭제할 수 있다.")
    @Test
    void delete() {
        // given
        int hostUserNumber = 1;
        Location location = locationRepository.save(createLocation("Seoul"));
        LocalDate dueDate = LocalDate.of(2024, 11, 4);
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, dueDate, TravelStatus.IN_PROGRESS));

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
        LocalDate dueDate = LocalDate.of(2024, 11, 4);
        Travel savedTravel = travelRepository.save(createTravel(hostUserNumber, location, dueDate, TravelStatus.IN_PROGRESS));

        // when //then
        assertThatThrownBy(() -> {
            travelService.delete(savedTravel.getNumber(), requestUserNumber);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 삭제 권한이 없습니다.");
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

    private Travel createTravel(int hostUserNumber, Location location, LocalDate dueDate, TravelStatus status) {
        return Travel.builder()
                .userNumber(hostUserNumber)
                .location(location)
                .locationName(location.getLocationName())
                .title("여행 제목")
                .details("여행 내용")
                .viewCount(0)
                .maxPerson(2)
                .genderType(GenderType.MIXED)
                .dueDate(dueDate)
                .periodType(PeriodType.ONE_WEEK)
                .status(status)
                .build();
    }

}