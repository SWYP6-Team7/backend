package swyp.swyp6_team7.enrollment.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.dto.EnrollmentCreateRequest;
import swyp.swyp6_team7.enrollment.repository.EnrollmentRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.notification.repository.NotificationRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @MockBean
    private TravelRepository travelRepository;

    @MockBean
    private NotificationRepository notificationRepository;

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
        Travel targetTravel = createTravel(dueDate, TravelStatus.IN_PROGRESS);

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
        Travel targetTravel = createTravel(dueDate, TravelStatus.IN_PROGRESS);

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
        Travel targetTravel = createTravel(dueDate, TravelStatus.DELETED);

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


    private Location createLocation() {
        return Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
    }

    private Travel createTravel(LocalDate dueDate, TravelStatus status) {
        return Travel.builder()
                .number(1)
                .userNumber(1)
                .location(createLocation())
                .viewCount(0)
                .dueDate(dueDate)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(status)
                .build();
    }

}