package swyp.swyp6_team7.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.comment.domain.Comment;

import java.util.List;
import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    //c

    //r
    //댓글 조회 (댓글, 답글 전부 조회)
    List<Comment> findByRelatedTypeAndRelatedNumber(String relatedType, int relatedNumber);
    // 답글 개수 조회
    long countByRelatedTypeAndRelatedNumberAndParentNumber(String relatedType, int relatedNumber, int parentNumber);
    
    //댓글(혹은 답글) 한개 조회
    Optional<Comment> findByCommentNumber(Integer integer);

    //u

    // 좋아요 수 증가
//    @Modifying
//    @Transactional
//    @Query("UPDATE Comment c SET c.likes = c.likes + 1 WHERE c.commentNumber = :commentNumber")
//    int increaseLikes(int commentNumber);

    // 좋아요 수 감소
//    @Modifying
//    @Transactional
//    @Query("UPDATE Comment c SET c.likes = c.likes - 1 WHERE c.commentNumber = :commentNumber AND c.likes > 0")
//    int decreaseLikes(int commentNumber);

    //d
    //

    //내가 쓴 댓글 조회
//    List<Comment> findByUserNumber(int userNumber);
}