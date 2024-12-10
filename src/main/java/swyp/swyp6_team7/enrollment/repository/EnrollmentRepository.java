package swyp.swyp6_team7.enrollment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.swyp6_team7.enrollment.domain.Enrollment;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, EnrollmentCustomRepository {

    int countByTravelNumber(int travelNumber);

    long countByTravelNumberAndStatus(int travelNumber, EnrollmentStatus status);

    Enrollment findTopByUserNumberAndTravelNumberOrderByCreatedAtDesc(int userNumber, int travelNumber);

    Enrollment findOneByUserNumberAndTravelNumber(int userNumber, int travelNumber);

    boolean existsByUserNumberAndTravelNumber(int userNumber, int travelNumber);

    List<Enrollment> findByUserNumber(int userNumber);

}
