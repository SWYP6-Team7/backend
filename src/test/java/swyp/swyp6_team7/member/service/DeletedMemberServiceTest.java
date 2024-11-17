package swyp.swyp6_team7.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.member.entity.*;
import swyp.swyp6_team7.member.repository.DeletedUsersRepository;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class DeletedMemberServiceTest {

    @Autowired
    private MemberDeletedService memberDeletedService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private DeletedUsersRepository deletedUsersRepository;

    private Users testUser;

    private Users createTestUser() {
        Users user = new Users();
        user.setUserEmail("test@example.com");
        user.setUserName("Test User");
        user.setUserStatus(UserStatus.ABLE);
        user.setUserAgeGroup(AgeGroup.TEEN);
        user.setUserGender(Gender.F);
        user.setUserPw("testpw");
        return userRepository.save(user);
    }

    private SocialUsers createSocialUser(Users user) {
        SocialUsers socialUser = new SocialUsers();
        socialUser.setUser(user);
        socialUser.setSocialEmail("social@example.com");
        socialUser.setSocialLoginId("social123");
        return socialUser; // 소셜 사용자 저장
    }

    private Location createLocation(String name) {
        return locationRepository.findByLocationName(name)
                .orElseGet(() -> locationRepository.save(new Location(name, LocationType.DOMESTIC)));
    }

    private Travel createTravel(int number, Users user, Location location, String title) {
        return travelRepository.save(Travel.builder()
                .number(number)
                .userNumber(user.getUserNumber())
                .location(location)
                .locationName(location.getLocationName())
                .title(title)
                .genderType(GenderType.MAN_ONLY)
                .periodType(PeriodType.ONE_WEEK)
                .status(TravelStatus.IN_PROGRESS)
                .viewCount(0)
                .build());
    }

    @Test
    @Rollback(false)
    @DisplayName("삭제된 사용자 번호로 여행 목록 조회")
    public void testFindByDeletedUserNumber() {
        // Given
        Users testUser = createTestUser();
        Location location = createLocation("Seoul");

        DeletedUsers deletedUser = new DeletedUsers(
                "test@example.com",
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(2),
                testUser.getUserNumber()
        );
        deletedUsersRepository.save(deletedUser);
        deletedUsersRepository.flush();

        Travel travel = Travel.builder()
                .userNumber(testUser.getUserNumber())
                .location(location)
                .locationName(location.getLocationName())
                .title("Test Travel")
                .genderType(GenderType.MAN_ONLY)
                .periodType(PeriodType.ONE_WEEK)
                .status(TravelStatus.IN_PROGRESS)
                .deletedUser(deletedUser)  // 삭제된 사용자와 매핑
                .viewCount(0)
                .build();
        travelRepository.save(travel);
        travelRepository.flush();  // 즉시 반영

        // When
        List<Travel> travels = travelRepository.findByDeletedUserNumber(deletedUser.getUserNumber());

        // Then
        assertThat(travels).isNotEmpty();
        assertThat(travels.get(0).getDeletedUser()).isEqualTo(deletedUser);

    }

    @Test
    @DisplayName("탈퇴 회원 비식별화 테스트")
    void anonymizeDeletedUserTest() {
        Users testUser = createTestUser();
        SocialUsers socialUser = createSocialUser(testUser);

        memberDeletedService.deleteUserData(testUser, socialUser);

        Users deletedUser = userRepository.findById(testUser.getUserNumber()).orElseThrow();
        assertThat(deletedUser.getUserEmail()).isEqualTo("deleted@" + testUser.getUserNumber() + ".com");
        assertThat(deletedUser.getUserName()).isEqualTo("deletedUser");
        assertThat(deletedUser.getUserStatus()).isEqualTo(UserStatus.DELETED);

        assertThat(socialUser.getSocialEmail()).isEqualTo("deleted@" + testUser.getUserNumber() + ".com");
        assertThat(socialUser.getSocialLoginId()).isEqualTo("null");
    }



    @Test
    @DisplayName("탈퇴 후 3개월 동안 재가입 불가 테스트")
    void testReRegistrationNotAllowedWithin3Months() {
        // Given: 3개월 내에 탈퇴한 사용자의 이메일 설정
        Users testUser = createTestUser();
        DeletedUsers deletedUser = new DeletedUsers(
                testUser.getUserEmail(),
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(2),
                testUser.getUserNumber()
        );
        deletedUsersRepository.save(deletedUser);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            memberDeletedService.validateReRegistration(testUser.getUserEmail());
        });
    }

    @Test
    @DisplayName("탈퇴 회원 만료 삭제 테스트")
    void testRemovalOfExpiredDeletedUsers() {
        // Given: 만료된 탈퇴 사용자 데이터 삽입
        DeletedUsers expiredUser = new DeletedUsers();
        expiredUser.setUserNumber(1);
        expiredUser.setDeletedUserEmail("test@example.com");
        expiredUser.setDeletedUserDeleteDate(LocalDate.now().minusDays(10)); // 10일 전 삭제됨
        expiredUser.setFinalDeletionDate(LocalDate.now().minusDays(1)); // 어제 만료됨
        deletedUsersRepository.save(expiredUser);

        // When: 만료된 사용자를 삭제하는 로직 실행
        memberDeletedService.deleteExpiredUsers();

        // Then: 해당 사용자가 삭제되었는지 확인
        Optional<DeletedUsers> result = deletedUsersRepository.findById(expiredUser.getDeletedNumber());
        assertThat(result).isEmpty(); // 비어있어야 함

    }
}
