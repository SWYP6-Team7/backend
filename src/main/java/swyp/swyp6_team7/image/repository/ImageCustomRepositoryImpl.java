package swyp.swyp6_team7.image.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.image.domain.QImage;

@Slf4j
@Repository
public class ImageCustomRepositoryImpl implements ImageCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ImageCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    QImage image = QImage.image;


    @Override
    public boolean existsProfileImageByUserNumber(Integer userNumber) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(image)
                .where(
                        image.relatedType.eq("profile"),
                        image.relatedNumber.eq(userNumber),
                        image.order.eq(0)
                ).fetchFirst();

        return fetchOne != null;
    }

}
