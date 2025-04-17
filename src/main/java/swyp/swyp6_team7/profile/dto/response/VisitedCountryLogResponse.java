package swyp.swyp6_team7.profile.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import swyp.swyp6_team7.location.domain.Continent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class VisitedCountryLogResponse {
    private Integer userNumber;
    private Integer visitedCountriesCount;
    // 대륙별 (국가명, 시작일자) 목록
    private Map<Continent, List<CountryVisits>> internationalLogs;
    private List<DomesticVisit> domesticLogs;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CountryVisits {
        private String countryName;
        private List<LocalDate> visitDates;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DomesticVisit {
        private String locationName;
        private List<LocalDate> visitDates;
    }
}
