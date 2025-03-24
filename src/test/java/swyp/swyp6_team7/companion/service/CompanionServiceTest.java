package swyp.swyp6_team7.companion.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.companion.repository.CompanionRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserLoginHistoryRepository;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
class CompanionServiceTest {

    @Autowired
    private CompanionService companionService;

    @Autowired
    private CompanionRepository companionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TravelRepository travelRepository;
    @Autowired
    private UserLoginHistoryRepository userLoginHistoryRepository;


    @AfterEach
    void tearDown() {
        companionRepository.deleteAllInBatch();
        userLoginHistoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        travelRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
    }

    @DisplayName("findCompanionInfo: 특정 여행의 companion 정보를 가져올 수 있다.")
    @Test
    public void findCompanionInfoByTravelNumber() {
        // given
        Users user1 = userRepository.save(createUser("user1", AgeGroup.TEEN));
        Users user2 = userRepository.save(createUser("user2", AgeGroup.TWENTY));

        Location location = locationRepository.save(createLocation());
        Travel travel = travelRepository.save(createTravel(location));

        Companion companion1 = createCompanion(travel, user1.getUserNumber());
        Companion companion2 = createCompanion(travel, user2.getUserNumber());
        companionRepository.saveAll(List.of(companion1, companion2));

        // when
        List<CompanionInfoDto> companionInfo = companionService.findCompanionsByTravelNumber(travel.getNumber());

        // then
        assertThat(companionInfo).hasSize(2)
                .extracting("userName", "ageGroup", "profileUrl")
                .containsExactlyInAnyOrder(
                        tuple("user1", "10대", null),
                        tuple("user2", "20대", null)
                );
    }

    private Companion createCompanion(Travel travel, int userNumber) {
        return Companion.builder()
                .travel(travel)
                .userNumber(userNumber)
                .build();
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
                .startDate(LocalDate.of(2024, 11, 22))
                .endDate(LocalDate.of(2024, 11, 28))
                .viewCount(0)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(TravelStatus.IN_PROGRESS)
                .build();
    }

    private Users createUser(String userName, AgeGroup ageGroup) {
        return Users.builder()
                .userPw("1234")
                .userEmail(userName + "@test.com")
                .userName(userName)
                .userGender(Gender.M)
                .userAgeGroup(ageGroup)
                .userStatus(UserStatus.ABLE)
                .build();
    }

}