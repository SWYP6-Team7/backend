package swyp.swyp6_team7.plan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.plan.dto.PlanDetailDto;
import swyp.swyp6_team7.plan.dto.request.AllPlanUpdateRequest;
import swyp.swyp6_team7.plan.dto.request.PlanCreateRequest;
import swyp.swyp6_team7.plan.dto.request.PlanUpdateRequest;
import swyp.swyp6_team7.plan.dto.request.SpotRequest;
import swyp.swyp6_team7.plan.dto.response.PlanPagingResponse;
import swyp.swyp6_team7.plan.dto.response.PlanResponse;
import swyp.swyp6_team7.plan.entity.Plan;
import swyp.swyp6_team7.plan.entity.Spot;
import swyp.swyp6_team7.plan.repository.PlanRepository;
import swyp.swyp6_team7.plan.repository.SpotRepository;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class PlanService {
    private final PlanRepository planRepository;
    private final SpotRepository spotRepository;
    private final TravelRepository travelRepository;

    // 일정 단건 생성
    public PlanDetailDto create(Integer travelNumber, PlanCreateRequest request, Integer userNumber) {
        validateTravelHostUser(travelNumber, userNumber); // 여행 호스트 권한 확인

        Plan createdPlan = createPlanAndSpots(travelNumber, request.getPlanOrder(), request.getSpots());
        List<Spot> spots = spotRepository.findSpotsByPlanId(createdPlan.getId());
        return PlanDetailDto.from(createdPlan, spots);
    }

    // 일정 여러 건 생성
    public List<Plan> createPlans(Integer travelNumber, List<PlanCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return null;
        }

        return requests.stream()
                .map(planInfo -> createPlanAndSpots(travelNumber, planInfo.getPlanOrder(), planInfo.getSpots()))
                .toList();
    }

    // Plan 단건과 관련된 Spot 생성 및 저장
    private Plan createPlanAndSpots(Integer travelNumber, Integer planOrder, List<SpotRequest> spots) {
        if (planRepository.existsByTravelNumberAndOrder(travelNumber, planOrder)) {
            throw new MoingApplicationException("해당 날짜에 대한 일정이 이미 존재합니다.");
        }

        Plan newPlan = planRepository.save(
                Plan.create(travelNumber, planOrder)
        );

        List<Spot> newSpots = createNewSpots(newPlan.getId(), spots);
        log.info("여행 일정 및 장소 생성 완료: plan={}, spot size={}", newPlan, newSpots.size());

        return newPlan;
    }

    // 일정에 대한 방문 장소에 순서를 매겨 Spot 생성
    private List<Spot> createNewSpots(Long planId, List<SpotRequest> spots) {
        int order = 1;
        List<Spot> newSpots = new ArrayList<>();
        for (SpotRequest spotInfo : spots) {
            Spot spot = Spot.create(planId, order++, spotInfo.getLatitude(), spotInfo.getLongitude(),
                    spotInfo.getName(), spotInfo.getCategory(), spotInfo.getRegion());
            newSpots.add(spot);
        }
        return spotRepository.saveAll(newSpots);
    }

    // 일정 단건 조회
    @Transactional(readOnly = true)
    public PlanDetailDto findPlan(Integer travelNumber, Integer order) {
        return planRepository.findByTravelNumberAndOrder(travelNumber, order)
                .map(plan -> {
                    List<Spot> spots = spotRepository.findSpotsByPlanId(plan.getId());
                    return PlanDetailDto.from(plan, spots);
                })
                .orElse(null);
    }

    // 일정 페이징 조회
    @Transactional(readOnly = true)
    public PlanPagingResponse getPlans(Integer travelNumber, Integer cursor, Integer size) {
        List<Plan> plans = planRepository.getPlansWithNoOffsetPagination(travelNumber, cursor, size);

        List<Long> plansId = plans.stream()
                .map(Plan::getId)
                .toList();

        Map<Long, List<Spot>> spotMap = spotRepository.getSpotsByPlanIdIn(plansId).stream()
                .collect(Collectors.groupingBy(Spot::getPlanId));

        List<PlanResponse> planDetails = plans.stream()
                .map(plan -> PlanDetailDto.from(plan, spotMap.get(plan.getId())))
                .map(PlanResponse::from)
                .toList();

        // 다음 커서 설정 (현재 가져온 plan의 마지막 order)
        Integer nextCursor = plans.size() < size ? null : plans.get(plans.size() - 1).getOrder();

        return PlanPagingResponse.from(planDetails, nextCursor);
    }

    // 일정 단건 수정
    public PlanDetailDto update(Long planId, PlanUpdateRequest request, Integer userNumber) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new MoingApplicationException("일정이 존재하지 않습니다."));

        validateTravelHostUser(plan.getTravelNumber(), userNumber); // 여행 호스트 권한 확인

        // 예전 spot 삭제
        List<Spot> oldSpots = spotRepository.findSpotsByPlanId(plan.getId());
        spotRepository.deleteAll(oldSpots);

        //새로운 spot 생성
        List<Spot> newSpots = createNewSpots(plan.getId(), request.getSpots());
        log.info("여행 일정 및 장소 수정 완료: plan={}, spot size={}", plan, newSpots.size());

        return PlanDetailDto.from(plan, newSpots);
    }

    // 일정 여러 건 수정
    public void updatePlans(Integer travelNumber, AllPlanUpdateRequest request) {
        if (request.getAdded().isEmpty() & request.getUpdated().isEmpty() & request.getDeleted().isEmpty()) {
            return;
        }

        // 삭제
        if (!request.getDeleted().isEmpty()) {
            List<Plan> plans = planRepository.findAllByTravelNumberAndOrderIn(travelNumber, request.getDeleted());
            plans.stream()
                    .forEach(plan -> spotRepository.deleteSpotsByPlanId(plan.getId()));
            planRepository.deleteAll(plans);
            log.info("일정 삭제 완료: travelNumber={}, deleted order={}", travelNumber, request.getDeleted());
        }

        // 수정
        if (!request.getUpdated().isEmpty()) {
            for (AllPlanUpdateRequest.PlanInfo planInfo : request.getUpdated()) {
                Plan plan = planRepository.findByTravelNumberAndOrder(travelNumber, planInfo.getPlanOrder())
                        .orElseThrow(() -> new MoingApplicationException("일정이 존재하지 않습니다."));

                spotRepository.deleteSpotsByPlanId(plan.getId()); // 예전 spot 삭제
                List<Spot> newSpots = createNewSpots(plan.getId(), planInfo.getSpots()); // 새로운 spot 생성
                log.info("여행 일정 및 장소 수정 완료: plan={}, spot size={}", plan, newSpots.size());
            }
        }

        // 생성
        if (!request.getAdded().isEmpty()) {
            request.getAdded().stream()
                    .map(planInfo -> createPlanAndSpots(travelNumber, planInfo.getPlanOrder(), planInfo.getSpots()))
                    .toList();
        }
    }

    // 일정 단건 삭제
    public void delete(Long planId, Integer userNumber) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new MoingApplicationException("일정이 존재하지 않습니다."));

        validateTravelHostUser(plan.getTravelNumber(), userNumber); // 여행 호스트 권한 확인

        spotRepository.deleteSpotsByPlanId(plan.getId()); // 장소 삭제
        planRepository.delete(plan); // 일정 삭제
        log.info("여행 일정 삭제 완료: planId={}", plan.getId());
    }

    // 여행 일정 및 장소 전체 삭제
    public void deleteAllPlansAndRelatedSpots(Integer travelNumber) {
        List<Long> plansId = planRepository.getPlansIdByTravelNumber(travelNumber);
        try {
            spotRepository.deleteSpotsByPlanIdIn(plansId); // 관련 장소 삭제
            planRepository.deleteAllByIdInBatch(plansId); // 일정 삭제
            log.info("일정 전체 삭제 완료: travelNumber={}", travelNumber);
        } catch (Exception e) {
            log.warn("일정 전체 삭제 중 오류 발생: {}", e.getMessage());
            throw new MoingApplicationException("일정 삭제 도중 오류가 발생했습니다.");
        }
    }

    // 요청자가 여행 주최자인지 검증하는 메서드
    private void validateTravelHostUser(Integer travelNumber, Integer userNumber) {
        if (!travelRepository.existsTravelByNumberAndUserNumber(travelNumber, userNumber)) {
            throw new MoingApplicationException("해당 여행의 일정에 대한 권한이 없습니다.");
        }
    }

    public Integer getTravelPlanCount(Integer travelNumber) {
        return planRepository.getPlanCountByTravelNumber(travelNumber);
    }

}
