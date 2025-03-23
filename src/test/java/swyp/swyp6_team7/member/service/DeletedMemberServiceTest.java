package swyp.swyp6_team7.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletedMemberServiceTest {

    @InjectMocks
    private MemberDeletedService memberDeletedService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeletedUsersRepository deletedUsersRepository;

    @Mock
    private TravelRepository travelRepository;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setUserNumber(1);
        testUser.setUserEmail("test@example.com");
        testUser.setUserName("Test User");
        testUser.setUserStatus(UserStatus.ABLE);
        testUser.setUserAgeGroup(AgeGroup.TEEN);
        testUser.setUserGender(Gender.F);
        testUser.setUserPw("testpw");
    }

    @Test
    @DisplayName("삭제된 사용자 번호로 여행 목록 조회")
    void testFindByDeletedUserNumber() {
        // Given
        DeletedUsers deletedUser = new DeletedUsers(
                "test@example.com",
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(2),
                testUser.getUserNumber()
        );

        when(deletedUsersRepository.findByDeletedUserEmail(testUser.getUserEmail())).thenReturn(Optional.of(deletedUser));

        // When
        Optional<DeletedUsers> result = deletedUsersRepository.findByDeletedUserEmail(testUser.getUserEmail());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDeletedUserEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("탈퇴 회원 비식별화 테스트")
    void anonymizeDeletedUserTest() {
        // Given
        SocialUsers socialUser = new SocialUsers();
        socialUser.setUser(testUser);
        socialUser.setSocialEmail("social@example.com");
        socialUser.setSocialLoginId("social123");

        // When
        memberDeletedService.deleteUserData(testUser, socialUser);

        // Then
        assertThat(testUser.getUserEmail()).isEqualTo("deleted@" + testUser.getUserNumber() + ".com");
        assertThat(testUser.getUserName()).isEqualTo("deletedUser");
        assertThat(testUser.getUserStatus()).isEqualTo(UserStatus.DELETED);

        assertThat(socialUser.getSocialEmail()).isEqualTo("deleted@" + testUser.getUserNumber() + ".com");
        assertThat(socialUser.getSocialLoginId()).isEqualTo("null");

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("탈퇴 후 3개월 동안 재가입 불가 테스트")
    void testReRegistrationNotAllowedWithin3Months() {
        // Given
        DeletedUsers deletedUser = new DeletedUsers(
                testUser.getUserEmail(),
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(2),
                testUser.getUserNumber()
        );

        when(deletedUsersRepository.findAllByDeletedUserEmail("test@example.com"))
                .thenReturn(Optional.of(List.of(deletedUser)));

        // when & then - 예외 발생 검증
        assertThrows(IllegalArgumentException.class, () -> {
            memberDeletedService.validateReRegistration("test@example.com");
        });

        verify(deletedUsersRepository, times(1)).findAllByDeletedUserEmail("test@example.com");
    }

    @Test
    @DisplayName("탈퇴 회원 만료 삭제 테스트")
    void testRemovalOfExpiredDeletedUsers() {
        DeletedUsers expiredUser = new DeletedUsers();
        expiredUser.setUserNumber(1);
        expiredUser.setDeletedUserEmail("test@example.com");
        expiredUser.setDeletedUserDeleteDate(LocalDate.now().minusDays(10));
        expiredUser.setFinalDeletionDate(LocalDate.now().minusDays(1));

        when(deletedUsersRepository.findAllByFinalDeletionDateBefore(any()))
                .thenReturn(List.of(expiredUser));

        // 실행
        memberDeletedService.deleteExpiredUsers();

        // 객체가 같은지 확인하기 어려우므로, ID 기준으로 match
        verify(deletedUsersRepository).delete(argThat(deleted ->
                deleted.getUserNumber() == 1 &&
                        deleted.getDeletedUserEmail().equals("test@example.com")
        ));
    }
}
