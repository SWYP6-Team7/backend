package swyp.swyp6_team7.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.community.domain.Community;

import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {

    //게시물 상세 조회
    @Query("SELECT c FROM Community c WHERE c.postNumber = :postNumber")
    Optional<Community> findByPostNumber(@Param("postNumber") int postNumber);
    boolean existsByPostNumber(Integer postNumber);

}
