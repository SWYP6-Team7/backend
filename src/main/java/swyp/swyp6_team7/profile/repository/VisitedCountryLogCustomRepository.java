package swyp.swyp6_team7.profile.repository;

import com.querydsl.core.Tuple;

import java.util.List;

public interface VisitedCountryLogCustomRepository {
    List<Tuple> findVisitedCountriesWithContinentByUser(Integer userNumber);
}
