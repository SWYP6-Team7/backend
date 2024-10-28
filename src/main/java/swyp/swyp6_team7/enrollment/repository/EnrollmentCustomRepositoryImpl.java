package swyp.swyp6_team7.enrollment.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.enrollment.domain.EnrollmentStatus;
import swyp.swyp6_team7.enrollment.domain.QEnrollment;
import swyp.swyp6_team7.enrollment.dto.EnrollmentResponse;
import swyp.swyp6_team7.enrollment.dto.QEnrollmentResponse;
import swyp.swyp6_team7.image.domain.QImage;
import swyp.swyp6_team7.member.entity.QUsers;
import swyp.swyp6_team7.travel.domain.QTravel;

import java.util.List;


@Repository
public class EnrollmentCustomRepositoryImpl implements EnrollmentCustomRepository {

    private final JPAQueryFactory queryFactory;

    public EnrollmentCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    QEnrollment enrollment = QEnrollment.enrollment;
    QUsers users = QUsers.users;
    QTravel travel = QTravel.travel;
    QImage image = QImage.image;


    @Override
    public List<EnrollmentResponse> findEnrollmentsByTravelNumber(int travelNumber) {
        return queryFactory
                .select(new QEnrollmentResponse(
                        enrollment.number,
                        users.userName,
                        users.userAgeGroup,
                        image.url,
                        enrollment.createdAt,
                        enrollment.message,
                        enrollment.status
                ))
                .from(enrollment)
                .leftJoin(users).on(enrollment.userNumber.eq(users.userNumber))
                .leftJoin(image).on(enrollment.userNumber.eq(image.relatedNumber)
                        .and(image.relatedType.eq("profile")).and(image.order.eq(0)))
                .where(
                        enrollment.travelNumber.eq(travelNumber),
                        enrollment.status.eq(EnrollmentStatus.PENDING))
                .orderBy(enrollment.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Integer> findEnrolledUserNumbersByTravelNumber(int travelNumber) {
        return queryFactory
                .select(enrollment.userNumber)
                .from(enrollment)
                .where(
                        enrollment.travelNumber.eq(travelNumber),
                        enrollment.status.eq(EnrollmentStatus.ACCEPTED)
                ).fetch();
    }

    @Override
    public List<Tuple> findEnrollmentsByUserNumber(int userNumber) {
        return queryFactory
                .select(enrollment.number, travel.number, enrollment.status)
                .from(enrollment)
                .leftJoin(users).on(enrollment.userNumber.eq(users.userNumber))
                .leftJoin(travel).on(enrollment.travelNumber.eq(travel.number))
                .where(enrollment.userNumber.eq(userNumber))
                .orderBy(enrollment.createdAt.desc())
                .fetch();
    }
}
