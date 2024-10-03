package swyp.swyp6_team7.bookmark.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import swyp.swyp6_team7.bookmark.dto.BookmarkRequest;
import swyp.swyp6_team7.bookmark.dto.BookmarkResponse;
import swyp.swyp6_team7.bookmark.entity.Bookmark;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import swyp.swyp6_team7.location.domain.City;
import swyp.swyp6_team7.location.domain.CityType;

public class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private BookmarkService bookmarkService;
    @Mock
    private UserRepository userRepository;

    @Mock
    private TravelRepository travelRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("북마크 추가 - 북마크 개수 초과 시 오래된 북마크 삭제")
    public void testAddBookmark_MaxLimit() {
        // given
        BookmarkRequest request = new BookmarkRequest(1, 101);
        Users user = new Users();
        user.setUserNumber(1);
        City city = new City();
        city.setCityName("제주");
        city.setCityType(CityType.DOMESTIC);
        Travel travel = Travel.builder()
                .number(101)
                .location("제주")
                .city(city)
                .dueDate(LocalDate.now().plusDays(5))
                .build();
        Bookmark oldestBookmark = new Bookmark(1, 1, LocalDateTime.now().minusDays(10));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(travelRepository.findById(101)).thenReturn(Optional.of(travel));
        when(bookmarkRepository.countByUserNumber(1)).thenReturn(30);
        when(bookmarkRepository.findOldestByUserNumber(1)).thenReturn(List.of(oldestBookmark));

        // when
        bookmarkService.addBookmark(request);

        // then
        verify(bookmarkRepository, times(1)).delete(oldestBookmark);
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 추가 - 정상 추가")
    public void testAddBookmark_Normal() {
        // given
        BookmarkRequest request = new BookmarkRequest(1, 101);
        Users user = new Users();
        user.setUserNumber(1);
        City city = new City();
        city.setCityName("제주");
        city.setCityType(CityType.DOMESTIC);
        Travel travel = Travel.builder()
                .number(101)
                .location("제주")
                .city(city)
                .dueDate(LocalDate.now().plusDays(5))
                .build();


        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(travelRepository.findById(101)).thenReturn(Optional.of(travel));
        when(bookmarkRepository.countByUserNumber(1)).thenReturn(10);

        // when
        bookmarkService.addBookmark(request);

        // then
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 삭제")
    public void testRemoveBookmark() {
        // given
        Integer userNumber = 1;
        Integer travelNumber = 101;
        Bookmark bookmark = new Bookmark(userNumber, travelNumber, LocalDateTime.now());

        when(bookmarkRepository.findBookmarksByUserNumber(userNumber)).thenReturn(List.of(bookmark));

        // when
        bookmarkService.removeBookmark(travelNumber, userNumber);

        // then
        verify(bookmarkRepository, times(1)).delete(bookmark);
    }

    @Test
    @DisplayName("사용자의 북마크 목록 조회")
    public void testGetBookmarksByUser() {
        // given
        Integer userNumber = 1;
        Bookmark bookmark = new Bookmark(userNumber, 101, LocalDateTime.now());
        City city = new City();
        city.setCityName("제주");
        city.setCityType(CityType.DOMESTIC);
        Travel travel = Travel.builder()
                .number(101)
                .title("Sample Travel")
                .location("제주")
                .city(city)
                .createdAt(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(5))
                .maxPerson(4)
                .build();

        Users user = new Users();
        user.setUserNumber(userNumber);
        user.setUserName("John Doe");

        when(bookmarkRepository.findBookmarksByUserNumber(userNumber)).thenReturn(List.of(bookmark));
        when(travelRepository.findById(101)).thenReturn(Optional.of(travel));
        when(userRepository.findById(userNumber)).thenReturn(Optional.of(user));

        // when
        Page<BookmarkResponse> responses = bookmarkService.getBookmarksByUser(userNumber, 0, 5);

        // then
        assertThat(responses.getContent()).hasSize(1);
        BookmarkResponse response = responses.getContent().get(0);
        assertThat(response.getTravelNumber()).isEqualTo(101);
        assertThat(response.getTitle()).isEqualTo("Sample Travel");
        assertThat(response.getUserName()).isEqualTo("John Doe");
    }
    @Test
    @DisplayName("사용자의 북마크 목록 조회 - 빈 리스트 반환")
    public void testGetBookmarksByUser_EmptyList() {
        // given
        Integer userNumber = 1;
        when(bookmarkRepository.findBookmarksByUserNumber(userNumber)).thenReturn(List.of());

        // when
        Page<BookmarkResponse> responses = bookmarkService.getBookmarksByUser(userNumber, 0, 5);

        // then
        assertThat(responses.getContent()).isEmpty();
    }
}
