package swyp.swyp6_team7.member.service;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.member.entity.UserLoginHistory;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserLoginHistoryRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserLoginHistoryService {

    private final UserLoginHistoryRepository userLoginHistoryRepository;

    public UserLoginHistoryService(UserLoginHistoryRepository userLoginHistoryRepository) {
        this.userLoginHistoryRepository = userLoginHistoryRepository;
    }

    @Transactional
    public void saveLoginHistory(Users user) {
        System.out.println("로그인 이력 저장 시도: " + user.getUserEmail()); // 로그인 시도하는 유저 이메일 로그 출력
        UserLoginHistory loginHistory = new UserLoginHistory();
        loginHistory.setUser(user);
        loginHistory.setHisLoginDate(LocalDateTime.now());
        userLoginHistoryRepository.save(loginHistory);  // 로그인 시 이력 저장
        System.out.println("로그인 이력 저장 완료"); // 저장 완료 로그 출력
    }

    @Transactional
    public void updateLogoutHistory(Users user) {
        // 마지막 로그인 기록을 찾아 로그아웃 시간을 업데이트
        Optional<UserLoginHistory> lastLoginOpt = userLoginHistoryRepository
                .findTopByUserOrderByHisLoginDateDesc(user);

        if (lastLoginOpt.isPresent()) {
            UserLoginHistory lastLogin = lastLoginOpt.get();
            if (lastLogin.getHisLogoutDate() == null) {  // 아직 로그아웃 기록이 없는 경우만 처리
                lastLogin.setHisLogoutDate(LocalDateTime.now());
                userLoginHistoryRepository.save(lastLogin);  // 로그아웃 시간 업데이트
            }
        }
    }
}
