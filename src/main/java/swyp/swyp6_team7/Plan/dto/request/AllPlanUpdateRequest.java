package swyp.swyp6_team7.Plan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AllPlanUpdateRequest {
    //    List<PlanInfo> added;
//    List<PlanInfo> updated;
//    List<Integer> deleted;  // 삭제할 일정의 order 리스트

    @Builder.Default
    List<PlanInfo> added = new ArrayList<>();
    @Builder.Default
    List<PlanInfo> updated = new ArrayList<>();
    @Builder.Default
    List<Integer> deleted = new ArrayList<>();  // 삭제할 일정의 order 리스트

    public Integer getPlanSizeChangeValue() {
        return added.size() - deleted.size();
    }

    @Getter
    @AllArgsConstructor
    public static class PlanInfo {
        @NotNull
        private Integer planOrder;
        @Valid
        @Size(min = 1, max = 10, message = "여행 장소는 최소 1개는 설정되어야 하며, 최대 10개까지 설정할 수 있습니다.")
        private List<SpotRequest> spots;
    }
}
