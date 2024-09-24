package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TravelServiceTest {

    @Autowired
    private TravelService travelService;
    @Autowired
    private UserRepository userRepository;

    Users user;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .userNumber(1)
                .userEmail("test@naver.com")
                .userPw("1234")
                .userName("username")
                .userGender(Users.Gender.M)
                .userAgeGroup(Users.AgeGroup.TEEN)
                .userRegDate(LocalDateTime.now())
                .userStatus(Users.MemberStatus.ABLE)
                .build();
    }

    @DisplayName("create: 이메일로 유저를 가져와 여행 콘텐츠를 만들 수 있다")
    @Test
    public void createTravelWithUser() {
        // given
        userRepository.save(user);
        TravelCreateRequest request = TravelCreateRequest.builder()
                .title("test travel post")
                .completionStatus(true)
                .build();

        // when
        TravelDetailResponse detailTravel = travelService.create(request, "test@naver.com");

        // then
        assertThat(detailTravel.getTitle()).isEqualTo(request.getTitle());
        assertThat(detailTravel.getUserNumber()).isEqualTo(user.getUserNumber());
        assertThat(detailTravel.getUserName()).isEqualTo(user.getUserName());
    }

}