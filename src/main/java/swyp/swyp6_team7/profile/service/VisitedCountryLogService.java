package swyp.swyp6_team7.profile.service;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.profile.dto.VisitedCountryLogResponse;
import swyp.swyp6_team7.profile.dto.VisitedCountryStats;
import swyp.swyp6_team7.profile.repository.VisitedCountryLogRepository;
import swyp.swyp6_team7.profile.util.DistanceCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitedCountryLogService {
    private final VisitedCountryLogRepository visitedCountryLogRepository;

    public VisitedCountryLogResponse getVisitedCountriesByUser(Integer userNumber) {
        List<Tuple> rawResult = visitedCountryLogRepository.findVisitedCountriesWithContinentByUser(userNumber);

        // Map<continent, Map<countryName, List<startDate>>>
        Map<Continent, Map<String, List<LocalDate>>> grouped = rawResult.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(1, Continent.class),
                        Collectors.groupingBy(
                                tuple -> tuple.get(0, String.class),
                                Collectors.mapping(
                                        tuple -> tuple.get(2, LocalDate.class),
                                        Collectors.toList()
                                )
                        )
                ));

        // Map<continent, List<CountryVisits>>로 변환
        Map<Continent, List<VisitedCountryLogResponse.CountryVisits>> result =
                grouped.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().entrySet().stream()
                                        .map(e -> VisitedCountryLogResponse.CountryVisits.builder()
                                                .countryName(e.getKey())
                                                .visitDates(e.getValue())
                                                .build())
                                        .toList()
                        ));

        int totalCount = result.values().stream()
                .mapToInt(List::size)
                .sum();

        return VisitedCountryLogResponse.builder()
                .userNumber(userNumber)
                .visitedCountriesCount(totalCount)
                .visitedCountriesByContinent(result)
                .build();
    }

    // 여행 거리, 방문한 국가 수 계산
    public VisitedCountryStats calculateVisitedCountryStats(Integer userNumber) {
        List<Tuple> visits = visitedCountryLogRepository.findVisitedCountriesWithStartDate(userNumber);

        double baseLat = 37.5665, baseLon = 126.9780;

        double travelDistance = visits.stream()
                .map(tuple -> tuple.get(2, Country.class))
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .mapToDouble(c -> DistanceCalculator.calculateDistance(baseLat, baseLon, c.getLatitude(), c.getLongitude()))
                .sum();

        Set<String> uniqueCountries = visits.stream()
                .map(tuple -> tuple.get(0, String.class)) // country_name
                .collect(Collectors.toSet());

        return new VisitedCountryStats(travelDistance, uniqueCountries.size());
    }

}
