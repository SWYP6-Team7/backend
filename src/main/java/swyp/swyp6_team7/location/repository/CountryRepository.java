package swyp.swyp6_team7.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.swyp6_team7.location.domain.Country;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    boolean existsByCountryName(String countryName);
    Optional<Country> findByCountryName(String countryName);
}
