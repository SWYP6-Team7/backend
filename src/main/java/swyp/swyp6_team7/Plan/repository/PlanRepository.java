package swyp.swyp6_team7.Plan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.Plan.entity.Plan;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByTravelNumberAndOrder(Integer travelNumber, Integer order);

    List<Plan> findAllByTravelNumberAndOrderIn(Integer travelNumber, List<Integer> order);

    @Query("select count(*) from Plan p where p.travelNumber = :travelNumber")
    Integer getPlanCountByTravelNumber(@Param("travelNumber") Integer travelNumber);

    boolean existsByTravelNumberAndOrder(Integer travelNumber, Integer order);
}
