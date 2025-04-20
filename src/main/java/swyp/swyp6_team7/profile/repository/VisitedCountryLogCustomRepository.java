package swyp.swyp6_team7.profile.repository;

import com.querydsl.core.Tuple;

import java.util.List;

public interface VisitedCountryLogCustomRepository {
    List<Tuple> findVisitedCountriesWithContinentByUser(Integer userNumber);
    List<Tuple> findVisitedCountriesWithStartDate(Integer userNumber);
    List<Tuple> findInternationalVisits(Integer userNumber);
    List<Tuple> findDomesticVisits(Integer userNumber);
    List<Integer> findAllUserNumbersWithTravelLog();
}
