package swyp.swyp6_team7.bookmark.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.bookmark.entity.Bookmark;
import swyp.swyp6_team7.config.DataConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DataConfig.class)
public class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Test
    @DisplayName("사용자 번호로 북마크 조회 테스트")
    public void testFindByUserNumber() {
        // Given
        Integer userNumber = 1;
        Bookmark bookmark1 = new Bookmark(userNumber, 101, LocalDateTime.now());
        Bookmark bookmark2 = new Bookmark(userNumber, 102, LocalDateTime.now().minusDays(1));
        bookmarkRepository.save(bookmark1);
        bookmarkRepository.save(bookmark2);

        // when
        List<Bookmark> bookmarks = bookmarkRepository.findByUserNumber(userNumber);

        // then
        assertThat(bookmarks).hasSize(2);
    }

    @DisplayName("특정 여행에 북마크한 사용자의 번호 리스트 조회 테스트")
    @Test
    void findUserNumberByTravelNumber() {
        // given
        int travelNumber = 100;
        Bookmark bookmark1 = new Bookmark(1, travelNumber, LocalDateTime.of(2024, 11, 22, 12, 0));
        Bookmark bookmark2 = new Bookmark(2, travelNumber, LocalDateTime.of(2024, 11, 22, 0, 0));
        Bookmark bookmark3 = new Bookmark(3, 101, LocalDateTime.of(2024, 11, 22, 0, 0));
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        // when
        List<Integer> bookmarkedUsersNumber = bookmarkRepository.findUserNumberByTravelNumber(travelNumber);

        // then
        assertThat(bookmarkedUsersNumber).hasSize(2)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("사용자 번호로 북마크 개수 조회 테스트")
    void testCountByUserNumber() {
        // given
        Integer userNumber = 1;
        bookmarkRepository.save(new Bookmark(userNumber, 101, LocalDateTime.now()));
        bookmarkRepository.save(new Bookmark(userNumber, 102, LocalDateTime.now()));

        // when
        int count = bookmarkRepository.countByUserNumber(userNumber);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 여행 게시물의 북마크 개수를 조회한다")
    void testCountByTravelNumber() {
        // given
        Integer userNumber1 = 1;
        Integer userNumber2 = 2;
        int travelNumber = 101;
        bookmarkRepository.save(new Bookmark(userNumber1, travelNumber, LocalDateTime.now()));
        bookmarkRepository.save(new Bookmark(userNumber2, travelNumber, LocalDateTime.now().minusDays(1)));

        // when
        int count = bookmarkRepository.countByTravelNumber(travelNumber);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("가장 오래된 북마크 조회 테스트")
    public void testFindOldestByUserNumber() {
        // given
        Integer userNumber = 1;
        Bookmark bookmark1 = new Bookmark(userNumber, 101, LocalDateTime.now().minusDays(5));
        Bookmark bookmark2 = new Bookmark(userNumber, 102, LocalDateTime.now().minusDays(2));
        bookmarkRepository.save(bookmark1);
        bookmarkRepository.save(bookmark2);

        // when
        List<Bookmark> oldestBookmarks = bookmarkRepository.findOldestByUserNumber(userNumber);

        // then
        assertThat(oldestBookmarks).hasSize(2);
        assertThat(oldestBookmarks.get(0)).isEqualTo(bookmark1);
    }

    @Test
    @DisplayName("특정 사용자가 특정 여행을 북마크했는지 확인한다")
    void testExistsByUserNumberAndTravelNumber() {
        // given
        Integer userNumber = 1;
        int travelNumber = 101;
        Bookmark bookmark = new Bookmark(userNumber, travelNumber, LocalDateTime.now());
        bookmarkRepository.save(bookmark);

        // when
        boolean exists = bookmarkRepository.existsByUserNumberAndTravelNumber(userNumber, travelNumber);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 사용자의 특정 여행 북마크를 삭제한다")
    void testDeleteByUserNumberAndTravelNumber() {
        // given
        Integer userNumber = 1;
        int travelNumber = 101;
        Bookmark bookmark = new Bookmark(userNumber, travelNumber, LocalDateTime.now());
        bookmarkRepository.save(bookmark);

        // when
        int deletedCount = bookmarkRepository.deleteByUserNumberAndTravelNumber(userNumber, travelNumber);

        // then
        assertThat(deletedCount).isEqualTo(1); // 삭제된 엔티티의 수가 1인지 확인

    }
}
