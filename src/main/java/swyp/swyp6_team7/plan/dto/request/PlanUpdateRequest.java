package swyp.swyp6_team7.plan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlanUpdateRequest {

    @Valid
    @Size(min = 1, max = 10, message = "여행 장소는 최소 1개는 설정되어야 하며, 최대 10개까지 설정할 수 있습니다.")
    private List<SpotRequest> spots;
}
