package swyp.swyp6_team7.companion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.travel.domain.Travel;

import java.util.List;
import java.util.Optional;


public interface CompanionRepository extends JpaRepository<Companion, Long>, CompanionCustomRepository {

    Optional<Companion> findByTravelAndUserNumber(Travel travel, int userNumber);

    @Query("select c.userNumber from Companion c where c.travel.number = :travelNumber")
    List<Integer> getUserNumbersByTravelNumber(Integer travelNumber);

    List<Companion> findByUserNumber(Integer userNumber);

    void deleteByTravelAndUserNumber(Travel travel, int userNumber);
}
