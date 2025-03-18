package swyp.swyp6_team7.plan.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.plan.entity.Plan;
import swyp.swyp6_team7.plan.entity.QPlan;

import java.util.List;

@Slf4j
@Repository
public class PlanCustomRepositoryImpl implements PlanCustomRepository {

    private final JPAQueryFactory queryFactory;
    QPlan plan = QPlan.plan;

    public PlanCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Plan> getPlansWithNoOffsetPagination(Integer travelNumber, Integer planOrder, Integer pageSize) {
        return queryFactory
                .select(plan)
                .from(plan)
                .where(
                        plan.travelNumber.eq(travelNumber),
                        greaterThanOrder(planOrder)
                )
                .orderBy(plan.order.asc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression greaterThanOrder(Integer planOrder) {
        if (planOrder == null) {
            return null;
        }
        return plan.order.gt(planOrder);
    }

}
