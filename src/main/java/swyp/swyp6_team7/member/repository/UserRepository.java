package swyp.swyp6_team7.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.swyp6_team7.member.entity.Users;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByUserEmail(String email);
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.tagPreferences WHERE u.userNumber = :userNumber")
    Optional<Users> findUserWithTags(@Param("userNumber") Integer userNumber);
    Optional<Users> findByUserNumber(int userNumber);



}
