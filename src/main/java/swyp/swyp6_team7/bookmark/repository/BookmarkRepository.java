package swyp.swyp6_team7.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.swyp6_team7.bookmark.entity.Bookmark;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer> {
    List<Bookmark> findByUserNumber(Integer userNumber);

    @Query("SELECT b.userNumber FROM Bookmark b WHERE b.travelNumber = :travelNumber")
    List<Integer> findUserNumberByTravelNumber(@Param("travelNumber") Integer travelNumber);

    int countByUserNumber(Integer userNumber);

    int countByTravelNumber(int travelNumber);

    // 가장 오래된 북마크 조회
    @Query("SELECT b FROM Bookmark b WHERE b.userNumber = :userNumber ORDER BY b.bookmarkDate ASC")
    List<Bookmark> findOldestByUserNumber(@Param("userNumber") Integer userNumber);

    @Query("SELECT b FROM Bookmark b WHERE b.userNumber = :userNumber")
    List<Bookmark> findBookmarksByUserNumber(@Param("userNumber") Integer userNumber);

    boolean existsByUserNumberAndTravelNumber(Integer userNumber, Integer travelNumber);

    int deleteByUserNumberAndTravelNumber(Integer userNumber, Integer travelNumber);

}
