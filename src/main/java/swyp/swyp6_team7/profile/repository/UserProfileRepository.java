package swyp.swyp6_team7.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.swyp6_team7.member.entity.Users;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByUserNumber(Integer userNumber);
}
