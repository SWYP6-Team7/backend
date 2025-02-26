package swyp.swyp6_team7.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.member.entity.UserBlockExplanation;

@Repository
public interface UserBlockExplanationRepository extends JpaRepository<UserBlockExplanation, Integer> {
}
