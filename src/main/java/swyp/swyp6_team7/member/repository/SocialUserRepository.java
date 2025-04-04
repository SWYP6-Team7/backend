package swyp.swyp6_team7.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.swyp6_team7.member.entity.SocialUsers;
import swyp.swyp6_team7.member.entity.Users;

import java.util.Optional;

public interface SocialUserRepository extends JpaRepository<SocialUsers, Integer> {
    Optional<SocialUsers> findBySocialLoginId(String socialLoginId);

    Optional<SocialUsers> findBySocialLoginIdAndSocialEmail(String socialLoginId, String email);

    boolean existsBySocialLoginId(String socialLoginId);

    Optional<SocialUsers> findByUser(Users user);
}
