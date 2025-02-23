package swyp.swyp6_team7.companion.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.companion.domain.Companion;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
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

@Import(DataConfig.class)
@DataJpaTest
class CompanionCustomRepositoryImplTest {

    @Autowired
    private CompanionRepository companionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TravelRepository travelRepository;


    @DisplayName("findCompanionInfo: 특정 여행에 참여 확정된 사용자의 정보를 가져온다.")
    @Test
    void findCompanionInfoByTravelNumber() {
        // given
        Users user1 = userRepository.save(createUser("user1", AgeGroup.TEEN));
        Users user2 = userRepository.save(createUser("user2", AgeGroup.TWENTY));

        Image image1 = createImage(user1.getUserNumber(), "user1-profile-image");
        Image image2 = createImage(user2.getUserNumber(), "user2-profile-image");
        imageRepository.saveAll(List.of(image1, image2));

        Location location = locationRepository.save(createLocation());
        Travel travel = travelRepository.save(createTravel(location));

        Companion companion1 = createCompanion(travel, user1.getUserNumber());
        Companion companion2 = createCompanion(travel, user2.getUserNumber());
        companionRepository.saveAll(List.of(companion1, companion2));

        // when
        List<CompanionInfoDto> companionInfo = companionRepository.findCompanionInfoByTravelNumber(travel.getNumber());

        // then
        assertThat(companionInfo).hasSize(2)
                .extracting("userName", "ageGroup", "profileUrl")
                .containsExactlyInAnyOrder(
                        tuple("user1", "10대", "user1-profile-image"),
                        tuple("user2", "20대", "user2-profile-image")
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

    private Image createImage(int userNumber, String url) {
        return Image.builder()
                .relatedType("profile")
                .order(0)
                .relatedNumber(userNumber)
                .url(url)
                .build();
    }
}
