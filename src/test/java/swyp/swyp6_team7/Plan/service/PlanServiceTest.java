package swyp.swyp6_team7.Plan.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import swyp.swyp6_team7.Plan.dto.PlanDetailDto;
import swyp.swyp6_team7.Plan.dto.request.PlanCreateRequest;
import swyp.swyp6_team7.Plan.dto.request.SpotRequest;
import swyp.swyp6_team7.Plan.entity.Plan;
import swyp.swyp6_team7.Plan.entity.Spot;
import swyp.swyp6_team7.Plan.repository.PlanRepository;
import swyp.swyp6_team7.Plan.repository.SpotRepository;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyInt;

@SpringBootTest
class PlanServiceTest {

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private SpotRepository spotRepository;

    @MockBean
    private TravelRepository travelRepository;

    @AfterEach
    void tearDown() {
        spotRepository.deleteAllInBatch();
        planRepository.deleteAllInBatch();
    }

    @DisplayName("여행 일정을 단건 생성할 수 있다.")
    @Test
    void create() {
        // given
        Integer travelNumber = 1;
        Integer planOrder = 1;
        PlanCreateRequest request = new PlanCreateRequest(
                planOrder,
                List.of(createSpotRequest("장소명1"), createSpotRequest("장소명2"))
        );

        BDDMockito.given(travelRepository.existsTravelByNumberAndUserNumber(anyInt(), anyInt()))
                .willReturn(true);

        // when
        PlanDetailDto result = planService.create(travelNumber, request, 1);

        // then
        assertThat(planRepository.findAll()).hasSize(1)
                .extracting("travelNumber", "order")
                .contains(tuple(1, 1));
        assertThat(spotRepository.findAll()).hasSize(2)
                .extracting("planId", "order", "latitude", "longitude", "name", "category", "region")
                .containsExactlyInAnyOrder(
                        tuple(result.getId(), 1, "37.556041", "126.972306", "장소명1", "카테고리", "리전"),
                        tuple(result.getId(), 2, "37.556041", "126.972306", "장소명2", "카테고리", "리전")
                );
    }

    @DisplayName("여러 개의 여행 일정을 생성할 수 있다.")
    @Test
    void createPlans() {
        // given
        Integer travelNumber = 1;
        PlanCreateRequest planInfo1 = new PlanCreateRequest(
                1,
                List.of(createSpotRequest("장소명1"), createSpotRequest("장소명2"))
        );
        PlanCreateRequest planInfo2 = new PlanCreateRequest(
                5,
                List.of(createSpotRequest("장소명3"))
        );
        List<PlanCreateRequest> requests = List.of(planInfo1, planInfo2);

        // when
        List<Plan> plans = planService.createPlans(travelNumber, requests);

        // then
        assertThat(planRepository.findAll()).hasSize(2)
                .extracting("travelNumber", "order")
                .contains(
                        tuple(1, 1),
                        tuple(1, 5)
                );
        assertThat(spotRepository.findAll()).hasSize(3);
    }

    @DisplayName("")
    @Test
    void update() {
        // given

        // when

        // then
    }

    @DisplayName("여행 일정을 단건 삭제할 수 있다.")
    @Test
    void delete() {
        // given
        Plan plan1 = planRepository.save(createPlan(10, 3));
        Plan plan2 = planRepository.save(createPlan(10, 5));
        Spot spot1 = createSpot(plan1.getId(), 0, "장소1");
        Spot spot2 = createSpot(plan1.getId(), 0, "장소2");
        Spot spot3 = createSpot(plan2.getId(), 0, "장소3");
        spotRepository.saveAll(List.of(spot1, spot2, spot3));

        BDDMockito.given(travelRepository.existsTravelByNumberAndUserNumber(anyInt(), anyInt()))
                .willReturn(true);

        // when
        planService.delete(plan1.getId(), 1);

        // then
        assertThat(planRepository.findAll()).hasSize(1);
        assertThat(spotRepository.findAll()).hasSize(1);
    }

    private SpotRequest createSpotRequest(String name) {
        return new SpotRequest(
                name, "카테고리", "리전", "37.556041", "126.972306"
        );
    }

    private Plan createPlan(Integer travelNumber, Integer order) {
        return Plan.builder()
                .travelNumber(travelNumber)
                .order(order)
                .build();
    }

    private Spot createSpot(Long planId, Integer order, String spotName) {
        return Spot.builder()
                .planId(planId)
                .order(order)
                .name(spotName)
                .latitude("37.556041")
                .longitude("126.972306")
                .build();
    }

}
