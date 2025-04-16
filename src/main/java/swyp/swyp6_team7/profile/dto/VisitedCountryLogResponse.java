package swyp.swyp6_team7.profile.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import swyp.swyp6_team7.location.domain.Continent;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class VisitedCountryLogResponse {
    private Integer userNumber;
    private Integer visitedCountriesCount;
    private Map<Continent, List<String>> visitedCountriesByContinent;
}
