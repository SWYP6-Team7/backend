package swyp.swyp6_team7.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_UserFound() {
        // Given
        Users mockUser = new Users();
        mockUser.setUserNumber(1);
        mockUser.setUserEmail("user@example.com");
        mockUser.setUserPw("password");

        when(userRepository.findByUserNumber(1)).thenReturn(Optional.of(mockUser));

        // When
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername("1");

        // Then
        assertNotNull(userDetails);
        assertEquals("1", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        verify(userRepository, times(1)).findByUserNumber(1);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Given
        when(userRepository.findByUserNumber(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("999"));

        verify(userRepository, times(1)).findByUserNumber(999);
    }
}
