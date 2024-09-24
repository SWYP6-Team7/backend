package swyp.swyp6_team7.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.swyp6_team7.member.entity.UserLoginHistory;
import swyp.swyp6_team7.member.entity.Users;

import java.util.Optional;

public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Integer> {
    Optional<UserLoginHistory> findTopByUserOrderByHisLoginDateDesc(Users user);

    @Query("SELECT h FROM UserLoginHistory h WHERE h.user = :user ORDER BY h.hisLoginDate DESC")
    Optional<UserLoginHistory> findLastLoginHistory(@Param("user") Users user);

}
