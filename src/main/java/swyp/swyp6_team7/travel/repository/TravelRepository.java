package swyp.swyp6_team7.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.swyp6_team7.travel.domain.Travel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TravelRepository extends JpaRepository<Travel, Integer>, TravelCustomRepository {
    Optional<Travel> findByNumber(Integer integer);

    List<Travel> findByUserNumber(int userNumber);

    @Query("SELECT t FROM Travel t JOIN FETCH t.deletedUser u WHERE u.userNumber = :deletedUserNumber")
    List<Travel> findByDeletedUserNumber(@Param("deletedUserNumber") Integer deletedUserNumber);

    @Query("SELECT t.enrollmentsLastViewedAt FROM Travel t WHERE t.number = :travelNumber")
    LocalDateTime getEnrollmentsLastViewedAtByNumber(@Param("travelNumber") int travelNumber);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Travel t SET t.enrollmentsLastViewedAt = :lastViewedAt WHERE t.number = :travelNumber")
    void updateEnrollmentsLastViewedAtByNumber(@Param("travelNumber") int travelNumber, @Param("lastViewedAt") LocalDateTime lastViewedAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Travel t SET t.viewCount = t.viewCount + 1 WHERE t.number = :travelNumber")
    void updateViewCountPlusOneByTravelNumber(@Param("travelNumber") int travelNumber);

}
