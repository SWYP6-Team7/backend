package swyp.swyp6_team7.travel.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(DataConfig.class)
@DataJpaTest
class TravelRepositoryTest {

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private LocationRepository locationRepository;


    @DisplayName("TravelNumber가 주어질 때, 해당 Travel의 enrollmentLastViewedAt를 가져올 수 있다.")
    @Test
    void getEnrollmentsLastViewedAtByNumber() {
        // given
        LocalDateTime viewedAt = LocalDateTime.of(2024, 11, 8, 12, 0);
        Travel travel = travelRepository.save(createTravel(0, viewedAt));

        // when
        LocalDateTime result = travelRepository.getEnrollmentsLastViewedAtByNumber(travel.getNumber());

        // then
        assertThat(result)
                .isEqualTo(LocalDateTime.of(2024, 11, 8, 12, 0));
    }

    @DisplayName("TravelNumber와 LocalDateTime이 주어질 때, 해당 Travel의 enrollmentLastViewedAt의 값을 수정한다.")
    @Test
    void updateEnrollmentsLastViewedAtByNumber() {
        // given
        LocalDateTime viewedAt = LocalDateTime.of(2024, 11, 8, 12, 0);
        Travel travel = travelRepository.save(createTravel(0, viewedAt));

        LocalDateTime updateViewedAt = LocalDateTime.of(2024, 11, 8, 15, 0);

        // when
        travelRepository.updateEnrollmentsLastViewedAtByNumber(travel.getNumber(), updateViewedAt);

        // then
        assertThat(travelRepository.findAll()).hasSize(1)
                .extracting("enrollmentsLastViewedAt")
                .contains(LocalDateTime.of(2024, 11, 8, 15, 0));
    }


    private Travel createTravel(int nowViewCount, LocalDateTime enrollmentLastViewdAt) {
        Location location = locationRepository.save(Location.builder()
                .locationName("여행지명")
                .locationType(LocationType.DOMESTIC)
                .build());

        return Travel.builder()
                .userNumber(1)
                .location(location)
                .startDate(LocalDate.of(2024, 11, 22))
                .endDate(LocalDate.of(2024, 11, 28))
                .viewCount(nowViewCount)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(TravelStatus.IN_PROGRESS)
                .enrollmentsLastViewedAt(enrollmentLastViewdAt)
                .build();
    }

}

