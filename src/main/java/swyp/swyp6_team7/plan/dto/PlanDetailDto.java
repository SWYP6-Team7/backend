package swyp.swyp6_team7.plan.dto;

import lombok.Builder;
import lombok.Getter;
import swyp.swyp6_team7.plan.entity.Plan;
import swyp.swyp6_team7.plan.entity.Spot;

import java.util.List;

@Getter
public class PlanDetailDto {

    private Long id;
    private Integer travelNumber;
    private Integer planOrder;
    private List<Spot> spots;

    @Builder
    public PlanDetailDto(Long id, Integer travelNumber, Integer planOrder, List<Spot> spots) {
        this.id = id;
        this.travelNumber = travelNumber;
        this.planOrder = planOrder;
        this.spots = spots;
    }

    public static PlanDetailDto from(Plan plan, List<Spot> spots) {
        return PlanDetailDto.builder()
                .id(plan.getId())
                .travelNumber(plan.getTravelNumber())
                .planOrder(plan.getOrder())
                .spots(spots)
                .build();
    }

    @Override
    public String toString() {
        return "PlanDetailDto{" +
                "id=" + id +
                ", travelNumber=" + travelNumber +
                ", planOrder=" + planOrder +
                ", spots.size=" + spots.size() +
                '}';
    }
}
