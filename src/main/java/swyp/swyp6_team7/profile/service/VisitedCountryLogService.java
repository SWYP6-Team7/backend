package swyp.swyp6_team7.profile.service;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.profile.dto.response.VisitedCountryLogResponse;
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
    private final MemberService memberService;

    public VisitedCountryLogResponse getVisitedCountriesByUser(Integer userNumber) {
        List<Tuple> international = visitedCountryLogRepository.findInternationalVisits(userNumber);
        List<Tuple> domestic = visitedCountryLogRepository.findDomesticVisits(userNumber);

        // 국제 로그 그룹핑
        Map<Continent, Map<String, List<LocalDate>>> groupedInternational = international.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(1, Continent.class),
                        Collectors.groupingBy(
                                tuple -> tuple.get(0, String.class),
                                Collectors.mapping(t -> t.get(2, LocalDate.class), Collectors.toList())
                        )
                ));

        Map<Continent, List<VisitedCountryLogResponse.CountryVisits>> internationalLogs = groupedInternational.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .map(e -> VisitedCountryLogResponse.CountryVisits.builder()
                                        .countryName(e.getKey())
                                        .visitDates(e.getValue())
                                        .build())
                                .collect(Collectors.toList())
                ));

        // 국내 로그 그룹핑
        Map<String, List<LocalDate>> groupedDomestic = domestic.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(0, String.class),
                        Collectors.mapping(t -> t.get(1, LocalDate.class), Collectors.toList())
                ));

        List<VisitedCountryLogResponse.DomesticVisit> domesticLogs = groupedDomestic.entrySet().stream()
                .map(e -> VisitedCountryLogResponse.DomesticVisit.builder()
                        .locationName(e.getKey())
                        .visitDates(e.getValue())
                        .build())
                .collect(Collectors.toList());

        int visitedCountryCount = internationalLogs.values().stream().mapToInt(List::size).sum();

        return VisitedCountryLogResponse.builder()
                .userNumber(userNumber)
                .visitedCountriesCount(visitedCountryCount)
                .internationalLogs(internationalLogs)
                .domesticLogs(domesticLogs)
                .build();

    }

    // 방문한 국가 수 계산
    public int calculateVisitedCountryCount(Integer userNumber) {
        List<Tuple> visits = visitedCountryLogRepository.findVisitedCountriesWithStartDate(userNumber);

        Set<String> uniqueCountries = visits.stream()
                .map(tuple -> tuple.get(0, String.class)) // country_name
                .collect(Collectors.toSet());

        return uniqueCountries.size();
    }

    // 여행 거리 누적
    public void updateTravelDistance(Integer userNumber) {
        List<Tuple> visits = visitedCountryLogRepository.findVisitedCountriesWithStartDate(userNumber);

        double baseLat = 37.5665, baseLon = 126.9780;

        double travelDistance = visits.stream()
                .filter(tuple -> {
                    LocalDate endDate = tuple.get(3, LocalDate.class);
                    return endDate != null && endDate.isBefore(LocalDate.now());
                })
                .map(tuple -> tuple.get(2, Country.class))
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .mapToDouble(c -> DistanceCalculator.calculateDistance(baseLat, baseLon, c.getLatitude(), c.getLongitude()))
                .sum();

        memberService.updateUserTravelDistance(userNumber, travelDistance);
    }

    // 전체 유저 대상 여행 거리 누적
    @Scheduled(cron = "0 0 0 * * *")
    public void updateAllUsersTravelDistance() {
        List<Integer> userNumbers = visitedCountryLogRepository.findAllUserNumbersWithTravelLog();
        for (Integer userNumber : userNumbers) {
            updateTravelDistance(userNumber);
        }
    }


}
