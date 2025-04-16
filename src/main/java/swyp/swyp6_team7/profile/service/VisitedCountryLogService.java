package swyp.swyp6_team7.profile.service;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.profile.dto.VisitedCountryLogResponse;
import swyp.swyp6_team7.profile.repository.VisitedCountryLogRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitedCountryLogService {
    private final VisitedCountryLogRepository visitedCountryLogRepository;

    public VisitedCountryLogResponse getVisitedCountriesByUser(Integer userNumber) {
        List<Tuple> rawResult = visitedCountryLogRepository.findVisitedCountriesWithContinentByUser(userNumber);

        Map<Continent, List<String>> grouped = rawResult.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(1, Continent.class), // continent
                        Collectors.mapping(tuple -> tuple.get(0, String.class), Collectors.toList()) // country_name
                ));

        long totalCount = grouped.values().stream().flatMap(List::stream).distinct().count();

        return VisitedCountryLogResponse.builder()
                .userNumber(userNumber)
                .visitedCountriesCount((int) totalCount)
                .visitedCountriesByContinent(grouped)
                .build();
    }
}
