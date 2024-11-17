package swyp.swyp6_team7.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class PasswordChangeServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordChangeService passwordChangeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordChangeService = new PasswordChangeService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공 케이스")
    void testChangePassword_Success() {
        // given
        Integer userNumber = 1;
        String currentPassword = "correctCurrentPassword";
        String newPassword = "newPassword123";
        String newPasswordConfirm = "newPassword123";

        Users user = new Users();
        user.setUserNumber(userNumber);
        user.setUserPw("encodedCurrentPassword");

        // userRepository에서 사용자 찾기와 비밀번호 검증 설정
        when(userRepository.findById(userNumber)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getUserPw())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // when
        passwordChangeService.verifyCurrentPassword(userNumber, currentPassword);
        passwordChangeService.changePassword(userNumber, newPassword, newPasswordConfirm);

        // then
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        Users updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getUserPw()).isEqualTo("encodedNewPassword");
    }


    @Test
    @DisplayName("현재 비밀번호 검증 - 현재 비밀번호 불일치")
    void testVerifyCurrentPassword_InvalidCurrentPassword() {
        // given
        Integer userNumber = 1;
        String currentPassword = "wrongPassword";

        Users user = new Users();
        user.setUserNumber(userNumber);
        user.setUserPw("encodedCurrentPassword");

        when(userRepository.findById(userNumber)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getUserPw())).thenReturn(false);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordChangeService.verifyCurrentPassword(userNumber, currentPassword);
        });

        assertThat(exception.getMessage()).isEqualTo("현재 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 변경 - 새 비밀번호와 확인 비밀번호 불일치")
    void testChangePassword_NewPasswordsDoNotMatch() {
        // given
        Integer userNumber = 1;
        String newPassword = "newPassword123";
        String newPasswordConfirm = "differentNewPassword123";

        Users user = new Users();
        user.setUserNumber(userNumber);
        user.setUserPw("encodedCurrentPassword");

        when(userRepository.findById(userNumber)).thenReturn(Optional.of(user));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordChangeService.changePassword(userNumber, newPassword, newPasswordConfirm);
        });

        assertThat(exception.getMessage()).isEqualTo("새 비밀번호가 일치하지 않습니다.");
    }
}
