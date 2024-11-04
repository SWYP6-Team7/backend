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
        mockUser.setUserEmail("user@example.com");
        mockUser.setUserPw("password");

        when(userRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        // When
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername("user@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("user@example.com", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        verify(userRepository, times(1)).findByUserEmail("user@example.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Given
        when(userRepository.findByUserEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("unknown@example.com"));

        verify(userRepository, times(1)).findByUserEmail("unknown@example.com");
    }
}
