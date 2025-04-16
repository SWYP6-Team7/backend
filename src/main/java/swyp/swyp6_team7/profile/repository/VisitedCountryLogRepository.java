package swyp.swyp6_team7.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.swyp6_team7.travel.domain.Travel;

public interface VisitedCountryLogRepository extends JpaRepository<Travel, Integer>, VisitedCountryLogCustomRepository {
}
