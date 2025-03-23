package swyp.swyp6_team7.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.swyp6_team7.location.domain.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLocationName(String locationName);

    @Query("SELECT l FROM Location l WHERE l.locationName LIKE CONCAT(:prefix, '%')")
    List<Location> findByLocationNameStartingWith(@Param("prefix") String prefix);
}
