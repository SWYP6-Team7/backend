package swyp.swyp6_team7.profile.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.member.entity.AgeGroup;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.profile.dto.ProfileUpdateRequest;
import swyp.swyp6_team7.profile.repository.UserProfileRepository;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.UserTagPreference;
import swyp.swyp6_team7.tag.repository.TagRepository;
import swyp.swyp6_team7.tag.repository.UserTagPreferenceRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserTagPreferenceRepository userTagPreferenceRepository;
    @InjectMocks
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("프로필 업데이트 테스트 - 사용자 및 프로필 존재")
    void testUpdateProfile_Success() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("New Name");
        request.setAgeGroup(AgeGroup.TWENTY.getValue());
        request.setPreferredTags(new String[]{"Tag1", "Tag2"});

        Users user = new Users();
        user.setUserNumber(1);
        user.setUserName("Old Name");
        user.setUserAgeGroup(AgeGroup.TEEN);
        Set<UserTagPreference> tagPreferences = new HashSet<>();
        user.setTagPreferences(tagPreferences);

        when(userRepository.findUserWithTags(1)).thenReturn(Optional.of(user));
        when(tagRepository.findByName(anyString())).thenAnswer(invocation -> {
            String tagName = invocation.getArgument(0);
            Tag tag = Tag.of("testTag");
            tag.setName(tagName);
            return Optional.of(tag);
        });

        // when
        profileService.updateProfile(1, request);

        // then
        assertThat(user.getUserName()).isEqualTo("New Name");
        assertThat(user.getUserAgeGroup()).isEqualTo(AgeGroup.TWENTY);
        assertThat(tagPreferences).hasSize(2);
        verify(userRepository).save(user);
        verify(userTagPreferenceRepository).saveAll(anySet());
        verify(userRepository).flush();
        verify(userTagPreferenceRepository).flush();
    }

    @Test
    @DisplayName("프로필 업데이트 테스트 - 사용자 미존재")
    void testUpdateProfile_UserNotFound() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("New Name");

        when(userRepository.findUserWithTags(1)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.updateProfile(1, request);
        });

        assertThat(exception.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }



    @Test
    @DisplayName("userNumber로 프로필 조회 테스트")
    void testGetUserByUserNumber() {
        // given
        Users user = new Users();
        user.setUserNumber(1);
        when(userRepository.findUserWithTags(1)).thenReturn(Optional.of(user));

        // when
        Optional<Users> result = profileService.getUserByUserNumber(1);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserNumber()).isEqualTo(1);
    }


}
