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
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import swyp.swyp6_team7.location.domain.LocationType;

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
        Location travelLocation = new Location();
        travelLocation.setLocationName("제주");
        travelLocation.setLocationType(LocationType.DOMESTIC);
        Travel travel = Travel.builder()
                .number(101)
                .location(travelLocation)
                .locationName(travelLocation.getLocationName())
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
        Location travelLocation = new Location();
        travelLocation.setLocationName("제주");
        travelLocation.setLocationType(LocationType.DOMESTIC);
        Travel travel = Travel.builder()
                .number(101)
                .location(travelLocation)
                .locationName(travelLocation.getLocationName())
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

        // 모킹: 여행 정보를 조회
        Travel travel = Travel.builder()
                .number(travelNumber)
                .status(TravelStatus.IN_PROGRESS) // 삭제되지 않은 여행 상태 설정
                .build();

        when(travelRepository.findById(travelNumber)).thenReturn(Optional.of(travel));

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
        Location travelLocation = new Location();
        travelLocation.setLocationName("제주");
        travelLocation.setLocationType(LocationType.DOMESTIC);
        Travel travel = Travel.builder()
                .number(101)
                .location(travelLocation)
                .locationName(travelLocation.getLocationName())
                .title("Sample Travel")
                .createdAt(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(5))
                .maxPerson(4)
                .build();

        Users user = new Users();
        user.setUserNumber(userNumber);
        user.setUserName("John Doe");

        Users host = new Users();
        host.setUserNumber(travel.getUserNumber());
        host.setUserName("Host Name");

        when(bookmarkRepository.findBookmarksByUserNumber(userNumber)).thenReturn(List.of(bookmark));
        when(travelRepository.findById(101)).thenReturn(Optional.of(travel));
        when(userRepository.findByUserNumber(travel.getUserNumber())).thenReturn(Optional.of(host));

        // when
        Page<BookmarkResponse> responses = bookmarkService.getBookmarksByUser(userNumber, 0, 5);

        // then
        assertThat(responses.getContent()).hasSize(1);
        BookmarkResponse response = responses.getContent().get(0);
        assertThat(response.getTravelNumber()).isEqualTo(101);
        assertThat(response.getTitle()).isEqualTo("Sample Travel");
        assertThat(response.getUserName()).isEqualTo("Host Name");
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

    @DisplayName("주어지는 여행 목록에 대한 사용자의 북마크 여부 조회")
    @Test
    void getBookmarkExistenceByTravelNumbers() {
        // given
        int userNumber = 1;
        List<Integer> travelNumbers = List.of(1, 2, 3, 4);
        when(bookmarkRepository.findExistingBookmarkedTravelNumbers(userNumber, travelNumbers))
                .thenReturn(List.of(2, 3));

        // when
        Map<Integer, Boolean> bookmarkedMap = bookmarkService.getBookmarkExistenceByTravelNumbers(userNumber, travelNumbers);

        // then
        assertThat(bookmarkedMap).hasSize(4);
        assertThat(bookmarkedMap.get(1)).isEqualTo(false);
        assertThat(bookmarkedMap.get(2)).isEqualTo(true);
        assertThat(bookmarkedMap.get(3)).isEqualTo(true);
        assertThat(bookmarkedMap.get(4)).isEqualTo(false);
    }
    
}
