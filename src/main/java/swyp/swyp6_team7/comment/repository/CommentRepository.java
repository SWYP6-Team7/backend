package swyp.swyp6_team7.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.comment.domain.Comment;

import java.util.List;
import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    //댓글 조회 (댓글, 답글 전부 조회)
    @Query("SELECT DISTINCT c FROM Comment c WHERE c.relatedType = :relatedType AND c.relatedNumber = :relatedNumber")
    List<Comment> findByRelatedTypeAndRelatedNumber(@Param("relatedType") String relatedType, @Param("relatedNumber") int relatedNumber);

    @Query("SELECT c.commentNumber FROM Comment c WHERE c.relatedType = :relatedType AND c.relatedNumber = :relatedNumber")
    List<Integer> findCommentNumbersByRelatedTypeAndRelatedNumber(@Param("relatedType") String relatedType, @Param("relatedNumber") Integer relatedNumber);

    boolean existsByCommentNumber(Integer commentNumber);

    //특정 게시글의 댓글 개수 전부 조회
    long countByRelatedTypeAndRelatedNumber(String relatedType, int relatedNumber);

    @Modifying(clearAutomatically = true)
    @Query("delete from Comment c where c.relatedType = :relatedType and c.relatedNumber = :relatedNumber")
    void deleteCommentsByRelatedTypeAndRelatedNumber(@Param("relatedType") String relatedType, @Param("relatedNumber") Integer relatedNumber);

    // 답글 개수 조회
    long countByRelatedTypeAndRelatedNumberAndParentNumber(String relatedType, int relatedNumber, int parentNumber);

    //같은 부모를 가지는 댓글 조회
    List<Comment> findByRelatedTypeAndRelatedNumberAndParentNumber(String relatedType, int relatedNumber, int parentNumber);


    //댓글 번호로 댓글 삭제
    void deleteByCommentNumber(int commentNumber);

    //댓글(혹은 답글) 한개 조회
    Optional<Comment> findByCommentNumber(int commentNumber);

}