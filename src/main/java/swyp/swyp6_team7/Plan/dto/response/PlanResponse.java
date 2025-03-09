package swyp.swyp6_team7.Plan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import swyp.swyp6_team7.Plan.dto.PlanDetailDto;
import swyp.swyp6_team7.Plan.entity.Spot;

import java.util.List;

@Getter
public class PlanResponse {

    private Long planId;
    private Integer planOrder; // Day n
    private Integer spotCount; // 장소 개수
    private List<SpotResponse> spots;

    public PlanResponse(Long planId, Integer planOrder, List<SpotResponse> spots) {
        this.planId = planId;
        this.planOrder = planOrder;
        this.spotCount = spots.size();
        this.spots = spots;
    }

    @AllArgsConstructor
    @Getter
    static class SpotResponse {
        private Integer spotOrder;
        private String name;
        private String category;
        private String region;
        private String latitude;
        private String longitude;

        static SpotResponse from(Spot spot) {
            return new SpotResponse(spot.getOrder(), spot.getName(), spot.getCategory(), spot.getRegion(), spot.getLatitude(), spot.getLongitude());
        }
    }

    public static PlanResponse from(PlanDetailDto planDetail) {
        if (planDetail == null) {
            return null;
        }
        List spotResponse = planDetail.getSpots().stream()
                .map(SpotResponse::from)
                .toList();
        return new PlanResponse(planDetail.getId(), planDetail.getPlanOrder(), spotResponse);
    }
}
