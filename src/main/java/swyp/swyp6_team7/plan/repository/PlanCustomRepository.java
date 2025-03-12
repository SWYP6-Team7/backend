package swyp.swyp6_team7.plan.repository;

import swyp.swyp6_team7.plan.entity.Plan;

import java.util.List;

public interface PlanCustomRepository {

    List<Plan> getPlansWithNoOffsetPagination(Integer travelNumber, Integer planOrder, Integer pageSize);

}
