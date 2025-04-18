package swyp.swyp6_team7.global;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.auth.service.LoginFacade;
import swyp.swyp6_team7.config.RedisContainerConfig;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.CountryRepository;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.util.Optional;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(RedisContainerConfig.class)
@ActiveProfiles("test")
@Slf4j
public class IntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberService memberService;

    @Value("${custom.admin-secret-key}")
    private String adminSecretKey;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private LoginFacade loginFacade;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CountryRepository countryRepository;

    protected Users createUser(
            String userName,
            String password
    ) {
        String encodedPassword = passwordEncoder.encode(password);

        Users user = Users.builder()
                .userPw(encodedPassword)
                .userEmail(userName + "@test.com")
                .userName(userName)
                .userGender(Gender.M)
                .userAgeGroup(AgeGroup.TWENTY)
                .userStatus(UserStatus.ABLE)
                .build();

        userRepository.save(user);

        return user;
    }

    protected void createAdminUser(
            String userName,
            String password
    ) {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail(userName + "@test.com");
        requestDto.setPassword(password);
        requestDto.setName(userName);
        requestDto.setGender("M");
        requestDto.setAgegroup("TWENTY");
        requestDto.setAdminSecretKey(adminSecretKey);

        memberService.createAdmin(requestDto);
    }

    protected LoginTokenResponse login(
            String email,
            String password
    ) {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(email);
        loginRequestDto.setPassword(password);

        LoginTokenResponse tokenResponse = loginFacade.login(loginRequestDto);
        return tokenResponse;
    }
    protected Country getOrCreateCountry(String countryName) {
        return countryRepository.findByCountryName(countryName)
                .orElseGet(() -> countryRepository.save(
                        Country.builder()
                                .countryName(countryName)
                                .continent(Continent.ASIA)
                                .build()
                ));
    }

    protected Location createLocation(String locationName) {
        Optional<Location> locationPrev = locationRepository.findByLocationName(locationName);
        if (locationPrev.isPresent()) {
            return locationPrev.get();
        }

        Country country = getOrCreateCountry("countryName");

        Location newLocation = Location.builder()
                .locationName(locationName)
                .locationType(LocationType.UNKNOWN) // UNKNOWN으로 설정
                .country(country)
                .build();

        return locationRepository.save(newLocation);
    }

    protected Travel createTravel(int userNumber, String locationName) {
        Location location = createLocation(locationName);

        Travel travel = Travel.builder()
                .userNumber(userNumber)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .locationName(locationName)
                .location(location)
                .title("여행 타이틀 " + userNumber)
                .details("여행 상세 " + userNumber)
                .viewCount(0)
                .maxPerson(3)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(TravelStatus.IN_PROGRESS)
                .build();

        travelRepository.save(travel);
        return travel;
    }

    protected void deleteTravel(int travelId) {
        travelRepository.deleteById(travelId);
    }
}
