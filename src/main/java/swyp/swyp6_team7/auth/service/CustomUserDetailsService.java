package swyp.swyp6_team7.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.auth.details.CustomUserDetails;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userNumber) throws UsernameNotFoundException {
        try {
            Users user = userRepository.findByUserNumber(Integer.parseInt(userNumber))
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다 - userNumber: " + userNumber));

            // CustomUserDetails를 반환하도록 수정
            return new CustomUserDetails(user);
        } catch (NumberFormatException ex) {
            throw new UsernameNotFoundException("유효하지 않은 userNumber 형식: " + userNumber, ex);
        }
    }
}
