package swyp.swyp6_team7.member.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.member.entity.UserLoginHistory;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserLoginHistoryRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserLoginHistoryService {

    private final UserLoginHistoryRepository userLoginHistoryRepository;

    public UserLoginHistoryService(UserLoginHistoryRepository userLoginHistoryRepository) {
        this.userLoginHistoryRepository = userLoginHistoryRepository;
    }

    @Transactional
    public void saveLoginHistory(Users user) {
        log.info("로그인 이력 저장 요청: userNumber={}", user.getUserNumber());
        try {
            UserLoginHistory loginHistory = new UserLoginHistory();
            loginHistory.setUser(user);
            loginHistory.setHisLoginDate(LocalDateTime.now());
            userLoginHistoryRepository.save(loginHistory);
            log.info("로그인 이력 저장 성공: userNumber={}", user.getUserNumber());
        } catch (Exception e) {
            log.error("로그인 이력 저장 실패: userNumber={}", user.getUserNumber(), e);
            throw new IllegalStateException("로그인 이력 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public void updateLogoutHistory(Users user) {
        log.info("로그아웃 이력 업데이트 요청: userNumber={}", user.getUserNumber());
        try {
            Optional<UserLoginHistory> lastLoginOpt = userLoginHistoryRepository.findTopByUserOrderByHisLoginDateDesc(user);

            if (lastLoginOpt.isPresent()) {
                UserLoginHistory lastLogin = lastLoginOpt.get();
                if (lastLogin.getHisLogoutDate() == null) {  // 로그아웃 기록이 없는 경우만 처리
                    lastLogin.setHisLogoutDate(LocalDateTime.now());
                    userLoginHistoryRepository.save(lastLogin);
                    log.info("로그아웃 시간 업데이트 성공: userNumber={}", user.getUserNumber());
                } else {
                    log.warn("이미 로그아웃 기록이 존재: userNumber={}, logoutDate={}", user.getUserNumber(), lastLogin.getHisLogoutDate());
                }
            } else {
                log.warn("로그아웃 기록 업데이트 실패 - 로그인 이력이 없음: userNumber={}", user.getUserNumber());
            }
        } catch (Exception e) {
            log.error("로그아웃 이력 업데이트 중 오류 발생: userNumber={}", user.getUserNumber(), e);
            throw new IllegalStateException("로그아웃 이력 업데이트 중 오류가 발생했습니다.", e);
        }
    }
}
