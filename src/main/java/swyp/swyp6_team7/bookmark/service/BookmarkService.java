package swyp.swyp6_team7.bookmark.service;


import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.dto.BookmarkRequest;
import swyp.swyp6_team7.bookmark.dto.BookmarkResponse;
import swyp.swyp6_team7.bookmark.entity.Bookmark;
import swyp.swyp6_team7.bookmark.repository.BookmarkRepository;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static swyp.swyp6_team7.bookmark.dto.BookmarkResponse.*;

@AllArgsConstructor
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final TravelRepository travelRepository;

    @Transactional
    public void addBookmark(BookmarkRequest request) {

        Users user = userRepository.findById(request.getUserNumber())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Travel travel = travelRepository.findById(request.getTravelNumber())
                .orElseThrow(() -> new IllegalArgumentException("여행 정보를 찾을 수 없습니다."));

        // 북마크 개수 제한 확인
        int bookmarkCount = bookmarkRepository.countByUserNumber(request.getUserNumber());
        if (bookmarkCount >= 30) {
            // 가장 오래된 북마크 삭제
            List<Bookmark> oldestBookmarks = bookmarkRepository.findOldestByUserNumber(request.getUserNumber());
            bookmarkRepository.delete(oldestBookmarks.get(0));
        }

        // 북마크 저장
        Bookmark bookmark = new Bookmark(
                user.getUserNumber(),
                travel.getNumber(),
                LocalDateTime.now() // bookmarkDate 설정
        );
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(Integer travelNumber, Integer userNumber) {
        List<Bookmark> bookmarks = bookmarkRepository.findBookmarksByUserNumber(userNumber);

        // 특정 travelNumber와 일치하는 북마크 찾기
        Bookmark bookmark = bookmarks.stream()
                .filter(b -> b.getTravelNumber().equals(travelNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다."));

        bookmarkRepository.delete(bookmark);
    }

    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getBookmarksByUser(Integer userNumber, Integer page, Integer size) {
        int currentPage = (page != null) ? page : 0;
        int currentSize = (size != null) ? size : 5;
        Pageable pageable = PageRequest.of(currentPage, currentSize);

        List<Bookmark> bookmarks = bookmarkRepository.findBookmarksByUserNumber(userNumber);

        // 저장된 콘텐츠가 없을 때 빈 page 객체 반환
        if (bookmarks.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 북마크 목록을 조회하면서 각 여행 정보 가져오기
        List<BookmarkResponse> responses = bookmarks.stream()
                .map(bookmark -> {
                    Integer travelNumber = bookmark.getTravelNumber();
                    System.out.println("Travel Number: " + travelNumber); // 로그 추가
                    

                    Travel travel = travelRepository.findById(travelNumber)
                            .orElseThrow(() -> new IllegalArgumentException("여행 정보를 찾을 수 없습니다."));

                    Users user = userRepository.findById(userNumber)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                    int currentApplicants = travel.getCompanions().size();

                    List<String> tags = travel.getTravelTags().stream()
                            .map(travelTag -> travelTag.getTag().getName())
                            .collect(Collectors.toList());

                    return new BookmarkResponse(
                            travel.getNumber(),
                            travel.getTitle(),
                            user.getUserNumber(),
                            user.getUserName(),
                            tags,
                            currentApplicants,
                            travel.getMaxPerson(),
                            travel.getCreatedAt(),
                            travel.getDueDate(),
                            true
                    );
                }).collect(Collectors.toList());

        int start = Math.min(currentPage * currentSize, responses.size());
        int end = Math.min((currentPage + 1) * currentSize, responses.size());

        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    @Transactional(readOnly = true)
    public List<Integer> getBookmarkedTravelNumbers(Integer userNumber) {
        // 사용자 번호로 북마크된 모든 여행 번호를 조회
        return bookmarkRepository.findBookmarksByUserNumber(userNumber).stream()
                .map(Bookmark::getTravelNumber)
                .collect(Collectors.toList());
    }

}
