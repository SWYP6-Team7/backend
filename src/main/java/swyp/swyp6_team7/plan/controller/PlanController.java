package swyp.swyp6_team7.plan.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.plan.dto.PlanDetailDto;
import swyp.swyp6_team7.plan.dto.request.PlanCreateRequest;
import swyp.swyp6_team7.plan.dto.request.PlanUpdateRequest;
import swyp.swyp6_team7.plan.dto.response.PlanResponse;
import swyp.swyp6_team7.plan.service.PlanService;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PlanController {

    private final PlanService planService;

    // 일정 단건 생성
    @PostMapping("/api/travel/{travelNumber}/plan")
    public ApiResponse<PlanResponse> create(
            @PathVariable(name = "travelNumber") Integer travelNumber,
            @RequestBody @Valid PlanCreateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        PlanDetailDto planDetail = planService.create(travelNumber, request, userNumber);
        PlanResponse response = PlanResponse.from(planDetail);
        return ApiResponse.success(response);
    }

    // 일정 단건 조회
    @GetMapping("/api/travel/{travelNumber}/plans")
    public ApiResponse<PlanResponse> getPlan(
            @PathVariable(name = "travelNumber") Integer travelNumber,
            @RequestParam(name = "order") Integer order
    ) {
        PlanDetailDto planDetail = planService.findPlan(travelNumber, order);
        PlanResponse response = PlanResponse.from(planDetail);
        return ApiResponse.success(response);
    }

    // 일정 단건 수정
    @PutMapping("/api/plan/{planId}")
    public ApiResponse<PlanResponse> update(
            @PathVariable(name = "planId") Long planId,
            @RequestBody @Valid PlanUpdateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        PlanDetailDto planDetail = planService.update(planId, request, userNumber);
        PlanResponse response = PlanResponse.from(planDetail);
        return ApiResponse.success(response);
    }

    // 일정 단건 삭제
    @DeleteMapping("/api/plan/{planId}")
    public ApiResponse<String> delete(
            @PathVariable(name = "planId") Long planId,
            @RequireUserNumber Integer userNumber
    ) {
        planService.delete(planId, userNumber);
        return ApiResponse.success("여행 일정이 삭제되었습니다.");
    }

}
