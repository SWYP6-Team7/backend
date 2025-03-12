package swyp.swyp6_team7.plan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.plan.entity.Spot;

import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {

    @Query("select s from Spot s where s.planId = :planId ORDER BY s.order ASC")
    List<Spot> findSpotsByPlanId(@Param("planId") Long planId);

    @Query("select s from Spot s where s.planId in :plansId ORDER BY s.order ASC")
    List<Spot> getSpotsByPlanIdIn(@Param("plansId") List<Long> plansId);

    void deleteSpotsByPlanId(Long planId);
}
