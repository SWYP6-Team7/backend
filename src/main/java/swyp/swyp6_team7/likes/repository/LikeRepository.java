package swyp.swyp6_team7.likes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.likes.domain.Like;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    // 좋아요 누름 여부 확인
    boolean existsByRelatedTypeAndRelatedNumberAndUserNumber(String relatedType, int relatedNumber, int userNumber);

    // 좋아요 수 조회
    long countByRelatedTypeAndRelatedNumber(String relatedType, int relatedNumber);

    //삭제
    @Modifying(clearAutomatically = true)
    @Query("delete from Like l where l.relatedType = :relatedType and l.relatedNumber = :relatedNumber")
    void deleteByRelatedTypeAndRelatedNumber(@Param("relatedType") String relatedType, @Param("relatedNumber") Integer relatedNumber);

    @Modifying(clearAutomatically = true)
    @Query("delete from Like l where l.relatedType = 'comment' and l.relatedNumber in :relatedCommentNumbers")
    void deleteAllCommentLikesByRelatedNumberIn(@Param("relatedCommentNumbers") List<Integer> relatedCommentNumbers);

    //좋아요 행 조회
    Optional<Like> findByRelatedTypeAndRelatedNumberAndUserNumber(String relatedType, int relatedNumber, int userNumber);
}
