package swyp.swyp6_team7.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 현재 비밀번호 검증 로직
    @Transactional(readOnly = true)
    public void verifyCurrentPassword(Integer userNumber, String confirmPassword) {
        try {
            log.info("현재 비밀번호 확인 - userNumber: {}", userNumber);
            // 사용자 조회
            Users user = userRepository.findById(userNumber)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(confirmPassword, user.getUserPw())) {
                log.warn("Password verification failed for userNumber: {}", userNumber);
                throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
            }
            log.info("현재 비밀번호 확인 성공 - userNumber: {}",userNumber);
        } catch (Exception e) {
            log.error("현재 비밀번호 확인 중 에러발생 - userNumber : {}",userNumber,e);
            throw e;
        }

    }

    // 비밀번호 변경 로직
    @Transactional
    public void changePassword(Integer userNumber, String newPassword, String newPasswordConfirm) {
        try{
            log.info("비밀번호 변경 시작 - userNumber : {}",userNumber);
            // 사용자 조회
            Users user = userRepository.findById(userNumber)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
            if (!newPassword.equals(newPasswordConfirm)) {
                log.warn("비밀번호 불일치 - userNumber: {}", userNumber);
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호 암호화 후 저장 (Argon2 사용)
            user.setUserPw(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("비밀번호 변경 성공 - userNumber: {}", userNumber);
        } catch (Exception e){
            log.error("비밀번호 변경 중 에러발생 - userNumber: {}", userNumber);
            throw e;
        }
    }
}
