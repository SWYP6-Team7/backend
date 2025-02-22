package swyp.swyp6_team7.enrollment.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.dto.EnrollmentResponse;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@Import(DataConfig.class)
@DataJpaTest
class EnrollmentCustomRepositoryImplTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ImageRepository imageRepository;


    @DisplayName("findEnrollments: 여행 번호가 주어질 때, 여행 참가 신청과 신청자 정보 목록을 가져올 수 있다.")
    @DirtiesContext
    @Test
    void findEnrollmentsByTravelNumber() {
        // given
        Users user1 = userRepository.save(createUser("user1"));
        Users user2 = userRepository.save(createUser("user2"));

        Image image1 = imageRepository.save(
                createImage(user1.getUserNumber(), "user1-profile-image")
        );

        Location location = locationRepository.save(createLocation());
        Travel travel = travelRepository.save(
                createTravel(3, 2, location, TravelStatus.IN_PROGRESS)
        );

        Enrollment enrollment1 = createEnrollment(travel.getNumber(), user1.getUserNumber(), EnrollmentStatus.PENDING);
        Enrollment enrollment2 = createEnrollment(travel.getNumber(), user2.getUserNumber(), EnrollmentStatus.PENDING);
        enrollmentRepository.saveAll(List.of(enrollment1, enrollment2));

        // when
        List<EnrollmentResponse> enrollments = enrollmentRepository.findEnrollmentsByTravelNumber(travel.getNumber());

        // then
        assertThat(enrollments).hasSize(2)
                .extracting("userName", "userAgeGroup", "profileUrl", "message", "status")
                .containsExactlyInAnyOrder(
                        tuple("user1", AgeGroup.TWENTY.getValue(), "user1-profile-image", "여행 참가 신청", "대기"),
                        tuple("user2", AgeGroup.TWENTY.getValue(), null, "여행 참가 신청", "대기")
                );
    }

    @DisplayName("findEnrollments: 여행 번호가 주어질 때, PENDING 상태의 여행 참가 신청 정보만 가져온다.")
    @DirtiesContext
    @Test
    void findEnrollmentsByTravelNumberOnlyPendingStatus() {
        // given
        Users user1 = userRepository.save(createUser("user1"));
        Users user2 = userRepository.save(createUser("user2"));

        Location location = locationRepository.save(createLocation());
        Travel travel = travelRepository.save(
                createTravel(3, 2, location, TravelStatus.IN_PROGRESS)
        );

        Enrollment enrollment1 = createEnrollment(travel.getNumber(), user1.getUserNumber(), EnrollmentStatus.PENDING);
        Enrollment enrollment2 = createEnrollment(travel.getNumber(), user2.getUserNumber(), EnrollmentStatus.ACCEPTED);
        enrollmentRepository.saveAll(List.of(enrollment1, enrollment2));

        // when
        List<EnrollmentResponse> enrollments = enrollmentRepository.findEnrollmentsByTravelNumber(travel.getNumber());

        // then
        assertThat(enrollments).hasSize(1)
                .extracting("userName", "userAgeGroup", "profileUrl", "message", "status")
                .contains(
                        tuple("user1", AgeGroup.TWENTY.getValue(), null, "여행 참가 신청", "대기")
                );
    }

    @DisplayName("findUserNumbers: 여행 번호와 신청 상태가 주어질 때, 해당하는 신청의 사용자 번호 목록을 가져온다.")
    @Test
    void findUserNumbersByTravelNumberAndStatus() {
        // given
        Users user1 = userRepository.save(createUser("user1"));
        Users user2 = userRepository.save(createUser("user2"));
        Users user3 = userRepository.save(createUser("user3"));

        Location location = locationRepository.save(createLocation());
        Travel travel = travelRepository.save(
                createTravel(3, 2, location, TravelStatus.IN_PROGRESS)
        );

        Enrollment enrollment1 = createEnrollment(travel.getNumber(), user1.getUserNumber(), EnrollmentStatus.PENDING);
        Enrollment enrollment2 = createEnrollment(travel.getNumber(), user2.getUserNumber(), EnrollmentStatus.ACCEPTED);
        Enrollment enrollment3 = createEnrollment(travel.getNumber(), user3.getUserNumber(), EnrollmentStatus.REJECTED);
        enrollmentRepository.saveAll(List.of(enrollment1, enrollment2, enrollment3));

        // when
        List<Integer> userNumbers = enrollmentRepository.findUserNumbersByTravelNumberAndStatus(travel.getNumber(), EnrollmentStatus.PENDING);

        // then
        assertThat(userNumbers).hasSize(1)
                .contains(user1.getUserNumber());
    }

    @DisplayName("특정 여행에 대해 특정 사용자의 대기 상태 신청 번호를 가져올 수 있다.")
    @Test
    void findPendingEnrollmentNumberByTravelNumberAndUserNumber() {
        // given
        Users user = userRepository.save(createUser("user1"));
        Location location = locationRepository.save(createLocation());
        Travel travel = travelRepository.save(
                createTravel(3, 2, location, TravelStatus.IN_PROGRESS)
        );

        Enrollment enrollment1 = enrollmentRepository.save(createEnrollment(travel.getNumber(), user.getUserNumber(), EnrollmentStatus.PENDING));
        Enrollment enrollment2 = enrollmentRepository.save(createEnrollment(travel.getNumber(), user.getUserNumber(), EnrollmentStatus.PENDING));

        // when
        Long result = enrollmentRepository.findPendingEnrollmentByTravelNumberAndUserNumber(travel.getNumber(), user.getUserNumber());

        // then
        assertThat(result).isEqualTo(enrollment2.getNumber());
    }

    private Enrollment createEnrollment(int travelNumber, int userNumber, EnrollmentStatus status) {
        return Enrollment.builder()
                .travelNumber(travelNumber)
                .userNumber(userNumber)
                .status(status)
                .message("여행 참가 신청")
                .createdAt(LocalDateTime.of(2024, 11, 13, 12, 0))
                .build();
    }

    private Image createImage(int userNumber, String url) {
        return Image.builder()
                .relatedType("profile")
                .order(0)
                .relatedNumber(userNumber)
                .url(url)
                .build();
    }

    private Location createLocation() {
        return Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
    }

    private Travel createTravel(int hostUserNumber, int maxPerson, Location location, TravelStatus status) {
        return Travel.builder()
                .userNumber(hostUserNumber)
                .maxPerson(maxPerson)
                .location(location)
                .viewCount(0)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(status)
                .build();
    }

    private Users createUser(String userName) {
        return Users.builder()
                .userPw("1234")
                .userEmail(userName + "@test.com")
                .userName(userName)
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .userStatus(UserStatus.ABLE)
                .build();
    }
}
