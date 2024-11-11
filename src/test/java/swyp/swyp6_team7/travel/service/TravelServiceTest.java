package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "kakao.client-id=fake-client-id",
        "kakao.client-secret=fake-client-secret",
        "kakao.redirect-uri=http://localhost:8080/login/oauth2/code/kakao",
        "kakao.token-url=https://kauth.kakao.com/oauth/token",
        "kakao.user-info-url=https://kapi.kakao.com/v2/user/me"
})
class TravelServiceTest {

    @Autowired
    private TravelService travelService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private TravelRepository travelRepository;
    
    Users user;

    @BeforeEach
    void setUp() {
        travelRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
        user = userRepository.save(Users.builder()
                .userEmail("test@naver.com")
                .userPw("1234")
                .userName("username")
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .userRegDate(LocalDateTime.now())
                .userStatus(UserStatus.ABLE)

                .build());

    }

    @DisplayName("create: 이메일로 유저를 가져와 여행 콘텐츠를 만들 수 있다")
    @Test
    @DirtiesContext
    public void createTravelWithUser() {
        // given
        Location travelLocation = Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
        Location savedLocation = locationRepository.save(travelLocation);
        TravelCreateRequest request = TravelCreateRequest.builder()
                .title("test travel post")
                .completionStatus(true)
                .locationName(savedLocation.getLocationName())
                .build();

        // when
        Travel createdTravel = travelService.create(request, , "test@naver.com");

        // then
        assertThat(createdTravel.getTitle()).isEqualTo(request.getTitle());
        assertThat(createdTravel.getUserNumber()).isEqualTo(user.getUserNumber());
        assertThat(createdTravel.getLocation().getLocationName()).isEqualTo(savedLocation.getLocationName());

    }

}