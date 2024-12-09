package swyp.swyp6_team7.enrollment.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;

import static org.assertj.core.api.Assertions.assertThat;

@Import(DataConfig.class)
@DataJpaTest
class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;


    @DisplayName("findTop: 사용자 번호와 여행 번호가 주어질 때, 가장 최신 신청을 조회한다.")
    @Test
    void findTopByUserNumberAndTravelNumberOrderByCreatedAtDesc() {
        // given
        int userNumber = 1;
        int travelNumber = 10;
        Enrollment enrollment1 = enrollmentRepository.save(createEnrollment(userNumber, travelNumber));
        Enrollment enrollment2 = enrollmentRepository.save(createEnrollment(userNumber, travelNumber));

        // when
        Enrollment enrollment = enrollmentRepository.findTopByUserNumberAndTravelNumberOrderByCreatedAtDesc(userNumber, travelNumber);

        // then
        assertThat(enrollment.getNumber()).isEqualTo(enrollment2.getNumber());
    }
    
    @DisplayName("findTop: 사용자 번호와 여행 번호가 주어질 때, 해당하는 신청이 없으면 null을 반환한다.")
    @Test
    void findTopByUserNumberAndTravelNumberOrderByCreatedAtDescWhenNotExist() {
        // given
        int userNumber = 1;
        int travelNumber = 10;

        // when
        Enrollment enrollment = enrollmentRepository.findTopByUserNumberAndTravelNumberOrderByCreatedAtDesc(userNumber, travelNumber);

        // then
        assertThat(enrollment).isNull();
    }

    private Enrollment createEnrollment(int userNumber, int travelNumber) {
        return Enrollment.builder()
                .userNumber(userNumber)
                .travelNumber(travelNumber)
                .status(EnrollmentStatus.PENDING)
                .build();
    }
}