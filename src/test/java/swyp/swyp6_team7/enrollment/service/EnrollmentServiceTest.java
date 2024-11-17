package swyp.swyp6_team7.enrollment.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.companion.repository.CompanionRepository;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.dto.EnrollmentCreateRequest;
import swyp.swyp6_team7.enrollment.dto.EnrollmentResponse;
import swyp.swyp6_team7.enrollment.repository.EnrollmentCustomRepositoryImpl;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.notification.repository.NotificationRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.dto.response.TravelEnrollmentsResponse;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @MockBean
    private EnrollmentCustomRepositoryImpl enrollmentCustomRepository;

    @MockBean
    private TravelRepository travelRepository;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private CompanionRepository companionRepository;

    @AfterEach
    void tearDown() {
        enrollmentRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
    }

    @DisplayName("create: 여행 번호가 주어질 때 참가 신청을 생성한다.")
    @Test
    void create() {
        // given
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(1, 2, dueDate, TravelStatus.IN_PROGRESS);

        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .travelNumber(targetTravel.getNumber())
                .message("여행 신청 본문")
                .build();
        int requestUserNumber = 2;
        LocalDate checkLocalDate = LocalDate.of(2024, 11, 10);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));

        // when
        enrollmentService.create(request, requestUserNumber, checkLocalDate);

        // then
        assertThat(enrollmentRepository.findAll()).hasSize(1)
                .extracting("userNumber", "travelNumber", "message", "status")
                .contains(
                        tuple(2, targetTravel.getNumber(), "여행 신청 본문", EnrollmentStatus.PENDING)
                );
    }

    @DisplayName("create: 마감 날짜를 넘겨 참가 신청할 경우 예외가 발생한다.")
    @Test
    void createWhenNotAvailableForEnroll() {
        // given
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(1, 2, dueDate, TravelStatus.IN_PROGRESS);

        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .travelNumber(targetTravel.getNumber())
                .message("여행 신청 본문")
                .build();
        int requestUserNumber = 2;
        LocalDate checkLocalDate = LocalDate.of(2024, 11, 12);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));

        // when // then
        assertThatThrownBy(() -> {
            enrollmentService.create(request, requestUserNumber, checkLocalDate);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("참가 신청 할 수 없는 상태의 여행입니다.");
    }

    @DisplayName("create: 여행 상태가 IN_PROGRESS가 아닌 경우 예외가 발생한다.")
    @Test
    void createWhenNotInProgressStatus() {
        // given
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(1, 2, dueDate, TravelStatus.DELETED);

        EnrollmentCreateRequest request = EnrollmentCreateRequest.builder()
                .travelNumber(targetTravel.getNumber())
                .message("여행 신청 본문")
                .build();
        int requestUserNumber = 2;
        LocalDate checkLocalDate = LocalDate.of(2024, 11, 12);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));

        // when // then
        assertThatThrownBy(() -> {
            enrollmentService.create(request, requestUserNumber, checkLocalDate);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("참가 신청 할 수 없는 상태의 여행입니다.");
    }

    @DisplayName("delete: 신청자 본인은 여행 참가 신청을 취소할 수 있다.")
    @Test
    void deleteWhenEnrolledUser() {
        // given
        Enrollment enrollment = createEnrollment(1, 1, "신청", EnrollmentStatus.PENDING);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // when
        enrollmentService.delete(savedEnrollment.getNumber(), 1);

        // then
        assertThat(enrollmentRepository.findAll()).hasSize(0);
    }

    @DisplayName("delete: 신청자가 아닌 경우 여행 참가 취소 요청 시 예외가 발생한다.")
    @Test
    void deleteWhenNotEnrolledUser() {
        // given
        Enrollment enrollment = createEnrollment(1, 1, "신청", EnrollmentStatus.PENDING);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // when // then
        assertThatThrownBy(() -> {
            enrollmentService.delete(savedEnrollment.getNumber(), 2);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 참가 신청 취소 권한이 없습니다.");
    }

    @DisplayName("accept: 주최자는 여행 참가 신청을 수락할 수 있다.")
    @Test
    void accept() {
        // given
        Integer hostUserNumber = 1;
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(hostUserNumber, 2, dueDate, TravelStatus.DELETED);

        Enrollment enrollment = createEnrollment(2, 1, "신청", EnrollmentStatus.PENDING);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));
        given(companionRepository.save(any(Companion.class)))
                .willReturn(Companion.create(targetTravel, enrollment.getUserNumber()));

        // when
        enrollmentService.accept(savedEnrollment.getNumber(), hostUserNumber);

        // then
        assertThat(enrollmentRepository.findAll()).hasSize(1)
                .extracting("userNumber", "travelNumber", "status")
                .contains(
                        tuple(2, targetTravel.getNumber(), EnrollmentStatus.ACCEPTED)
                );
    }

    @DisplayName("accept: 여행 신청 수락 요청은 주최자가 아닌 경우 예외가 발생한다.")
    @Test
    void acceptWhenNotHostUser() {
        // given
        Integer hostUserNumber = 1;
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(hostUserNumber, 2, dueDate, TravelStatus.DELETED);

        Enrollment enrollment = createEnrollment(2, 1, "신청", EnrollmentStatus.PENDING);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));
        given(companionRepository.save(any(Companion.class)))
                .willReturn(Companion.create(targetTravel, enrollment.getUserNumber()));

        // when
        assertThatThrownBy(() -> {
            enrollmentService.accept(savedEnrollment.getNumber(), 3);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 참가 신청 수락 권한이 없습니다.");
    }

    @DisplayName("accept: 여행 신청 수락 요청 시 이미 모집 인원이 충분한 경우 예외가 발생한다.")
    @Test
    void acceptWhenFullCompanion() {
        // given
        Integer hostUserNumber = 1;
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(hostUserNumber, 0, dueDate, TravelStatus.DELETED);

        Enrollment enrollment = createEnrollment(2, 1, "신청", EnrollmentStatus.PENDING);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));
        given(companionRepository.save(any(Companion.class)))
                .willReturn(Companion.create(targetTravel, enrollment.getUserNumber()));

        // when
        assertThatThrownBy(() -> {
            enrollmentService.accept(savedEnrollment.getNumber(), hostUserNumber);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 참가 모집 인원이 마감되어 수락할 수 없습니다.");
    }

    @DisplayName("accept: 주최자는 여행 참가 신청을 거절할 수 있다.")
    @Test
    void reject() {
        // given
        Integer hostUserNumber = 1;
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(hostUserNumber, 2, dueDate, TravelStatus.DELETED);

        Enrollment enrollment = createEnrollment(2, 1, "신청", EnrollmentStatus.PENDING);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));
        given(companionRepository.save(any(Companion.class)))
                .willReturn(Companion.create(targetTravel, enrollment.getUserNumber()));

        // when
        enrollmentService.reject(savedEnrollment.getNumber(), hostUserNumber);

        // then
        assertThat(enrollmentRepository.findAll()).hasSize(1)
                .extracting("userNumber", "travelNumber", "status")
                .contains(
                        tuple(2, targetTravel.getNumber(), EnrollmentStatus.REJECTED)
                );
    }

    @DisplayName("accept: 여행 신청 거절 요청은 주최자가 아닌 경우 예외가 발생한다.")
    @Test
    void rejectWhenNotHostUser() {
        // given
        Integer hostUserNumber = 1;
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(hostUserNumber, 2, dueDate, TravelStatus.DELETED);

        Enrollment enrollment = createEnrollment(2, 1, "신청", EnrollmentStatus.PENDING);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));
        given(companionRepository.save(any(Companion.class)))
                .willReturn(Companion.create(targetTravel, enrollment.getUserNumber()));

        // when
        assertThatThrownBy(() -> {
            enrollmentService.reject(savedEnrollment.getNumber(), 3);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 참가 신청 거절 권한이 없습니다.");
    }

    @DisplayName("findEnrollments: 여행 번호가 주어질 때, 관련된 여행 참가 신청 정보를 가져온다.")
    @Test
    void findEnrollments() {
        // given
        Integer hostUserNumber = 1;
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(hostUserNumber, 2, dueDate, TravelStatus.DELETED);

        EnrollmentResponse enrollment1 = EnrollmentResponse.builder()
                .enrollmentNumber(1)
                .userName("유저명1")
                .ageGroup(AgeGroup.TEEN)
                .status(EnrollmentStatus.PENDING)
                .build();
        EnrollmentResponse enrollment2 = EnrollmentResponse.builder()
                .enrollmentNumber(2)
                .userName("유저명2")
                .ageGroup(AgeGroup.TEEN)
                .status(EnrollmentStatus.PENDING)
                .build();

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));
        given(enrollmentCustomRepository.findEnrollmentsByTravelNumber(anyInt()))
                .willReturn(Arrays.asList(enrollment1, enrollment2));

        // when
        TravelEnrollmentsResponse result = enrollmentService.findEnrollmentsByTravelNumber(targetTravel.getNumber(), hostUserNumber);

        // then
        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getEnrollments()).hasSize(2);
    }

    @DisplayName("findEnrollments: 여행 참가 신청 목록을 조회할 때, 주최자가 아니면 예외가 발생한다.")
    @Test
    void findEnrollmentsWhenNotHostUser() {
        Integer hostUserNumber = 1;
        LocalDate dueDate = LocalDate.of(2024, 11, 11);
        Travel targetTravel = createTravel(hostUserNumber, 2, dueDate, TravelStatus.DELETED);

        given(travelRepository.findByNumber(any(Integer.class)))
                .willReturn(Optional.of(targetTravel));

        // when
        assertThatThrownBy(() -> {
            enrollmentService.findEnrollmentsByTravelNumber(targetTravel.getNumber(), 2);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 참가 신청 조회 권한이 없습니다.");
    }

    private Enrollment createEnrollment(int userNumber, int travelNumber, String message, EnrollmentStatus status) {
        return Enrollment.builder()
                .userNumber(userNumber)
                .travelNumber(travelNumber)
                .message(message)
                .status(status)
                .build();
    }

    private Location createLocation() {
        return Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
    }

    private Travel createTravel(int hostUserNumber, int maxPerson, LocalDate dueDate, TravelStatus status) {
        return Travel.builder()
                .number(1)
                .userNumber(hostUserNumber)
                .maxPerson(maxPerson)
                .location(createLocation())
                .viewCount(0)
                .dueDate(dueDate)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(status)
                .build();
    }

}