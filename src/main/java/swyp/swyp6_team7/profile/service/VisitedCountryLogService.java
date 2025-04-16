package swyp.swyp6_team7.profile.service;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.profile.dto.VisitedCountryLogResponse;
import swyp.swyp6_team7.profile.repository.VisitedCountryLogRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
}
