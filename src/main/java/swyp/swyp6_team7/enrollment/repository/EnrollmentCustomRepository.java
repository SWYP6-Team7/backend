package swyp.swyp6_team7.enrollment.repository;

import com.querydsl.core.Tuple;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.dto.EnrollmentResponse;

import java.util.List;

public interface EnrollmentCustomRepository {

    List<EnrollmentResponse> findEnrollmentsByTravelNumber(int travelNumber);

    List<Integer> findUserNumbersByTravelNumberAndStatus(int travelNumber, EnrollmentStatus status);

    Long findPendingEnrollmentByTravelNumberAndUserNumber(int travelNumber, int userNumber);

    List<Tuple> findEnrollmentsByUserNumber(int userNumber);

}
