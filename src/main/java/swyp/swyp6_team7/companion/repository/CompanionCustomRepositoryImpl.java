package swyp.swyp6_team7.companion.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.companion.domain.QCompanion;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.companion.dto.QCompanionInfoDto;
import swyp.swyp6_team7.image.domain.QImage;
import swyp.swyp6_team7.member.entity.QUsers;

import java.util.List;

@Repository
public class CompanionCustomRepositoryImpl implements CompanionCustomRepository {

    private final JPAQueryFactory queryFactory;

    public CompanionCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    QCompanion companion = QCompanion.companion;
    QUsers users = QUsers.users;
    QImage image = QImage.image;


    @Override
    public List<CompanionInfoDto> findCompanionInfoByTravelNumber(int travelNumber) {
        return queryFactory
                .select(new QCompanionInfoDto(
                        users.userNumber,
                        users.userName,
                        users.userAgeGroup,
                        image.url
                ))
                .from(companion)
                .leftJoin(users).on(companion.userNumber.eq(users.userNumber))
                .leftJoin(image).on(companion.userNumber.eq(image.relatedNumber)
                        .and(image.relatedType.eq("profile")).and(image.order.eq(0)))
                .where(companion.travel.number.eq(travelNumber))
                .fetch();
    }

}
