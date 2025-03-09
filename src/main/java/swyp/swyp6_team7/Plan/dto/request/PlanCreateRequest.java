package swyp.swyp6_team7.Plan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class PlanCreateRequest {

    @NotNull
    private Integer planOrder;
    @Valid
    @Size(min = 1, max = 10, message = "여행 장소는 최소 1개는 설정되어야 하며, 최대 10개까지 설정할 수 있습니다.")
    private List<SpotRequest> spots;

    public PlanCreateRequest(Integer planOrder, List<SpotRequest> spots) {
        this.planOrder = planOrder;
        this.spots = spots;
    }
}
