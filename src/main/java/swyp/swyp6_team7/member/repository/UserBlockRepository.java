package swyp.swyp6_team7.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.member.entity.UserBlock;

import java.util.List;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Integer> {

    List<UserBlock> findAllByUserNumberOrderByRegTs(Integer userNumber);
}
