package swyp.swyp6_team7.plan.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class PlanPagingResponse {

    private List<PlanResponse> plans;
    private Integer nextCursor; // 다음 요청에 사용되는 커서

    public PlanPagingResponse(List<PlanResponse> planResponses, Integer nextCursor) {
        this.plans = planResponses;
        this.nextCursor = nextCursor;
    }

    public static PlanPagingResponse from(List<PlanResponse> planResponses, Integer nextCursor) {
        return new PlanPagingResponse(planResponses, nextCursor);
    }
}
