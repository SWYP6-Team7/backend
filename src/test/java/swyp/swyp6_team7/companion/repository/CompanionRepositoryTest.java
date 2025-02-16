package swyp.swyp6_team7.companion.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(DataConfig.class)
@DataJpaTest
class CompanionRepositoryTest {

    @Autowired
    private CompanionRepository companionRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TravelRepository travelRepository;


    @DisplayName("특정 여행에 대해 참가자 사용자 번호 목록을 가져올 수 있다.")
    @Test
    void findUserNumberByTravelNumber() {
        // given
        Location location = locationRepository.save(createLocation());
        Travel travel = travelRepository.save(createTravel(location));
        Companion companion = companionRepository.save(createCompanion(travel, 1));
        Companion companion2 = companionRepository.save(createCompanion(travel, 5));

        // when
        List<Integer> companionUserNumbers = companionRepository.getUserNumbersByTravelNumber(travel.getNumber());

        // then
        assertThat(companionUserNumbers).hasSize(2)
                .contains(1, 5);
    }

    private Location createLocation() {
        return Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
    }

    private Travel createTravel(Location location) {
        return Travel.builder()
                .userNumber(3)
                .maxPerson(2)
                .location(location)
                .viewCount(0)
                .dueDate(LocalDate.of(2024, 11, 16))
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(TravelStatus.IN_PROGRESS)
                .build();
    }

    private Companion createCompanion(Travel travel, int userNumber) {
        return Companion.builder()
                .travel(travel)
                .userNumber(userNumber)
                .build();
    }
}
