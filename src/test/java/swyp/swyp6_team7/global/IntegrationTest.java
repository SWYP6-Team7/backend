package swyp.swyp6_team7.global;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import swyp.swyp6_team7.auth.dto.LoginRequestDto;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.auth.service.LoginFacade;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Gender;
import swyp.swyp6_team7.member.entity.UserStatus;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private TravelRepository travelRepository;

    @Autowired
    private LoginFacade loginFacade;

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

    protected Travel createTravel(int hostNumber) {
        Travel travel = Travel.builder()
                .userNumber(hostNumber)
                .build();

        travelRepository.save(travel);
        return travel;
    }
}
