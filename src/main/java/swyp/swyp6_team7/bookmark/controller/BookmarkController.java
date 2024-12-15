package swyp.swyp6_team7.bookmark.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.bookmark.dto.BookmarkRequest;
import swyp.swyp6_team7.bookmark.dto.BookmarkResponse;
import swyp.swyp6_team7.bookmark.service.BookmarkService;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final JwtProvider jwtProvider;

    public BookmarkController(BookmarkService bookmarkService,JwtProvider jwtProvider) {
        this.bookmarkService = bookmarkService;
        this.jwtProvider = jwtProvider;
    }

    // 북마크 추가
    @PostMapping
    public ResponseEntity<?> addBookmark(@RequestHeader("Authorization") String token,@RequestBody BookmarkRequest request) {
        // 토큰에서 userNumber 추출
        String jwtToken = token.replace("Bearer ", "");
        Integer userNumber = jwtProvider.getUserNumber(jwtToken);

        // userNumber를 요청에 추가
        request.setUserNumber(userNumber);
        bookmarkService.addBookmark(request);
        return ResponseEntity.ok().build();
    }

    // 북마크 삭제
    @DeleteMapping("/{travelNumber}")
    public ResponseEntity<?> removeBookmark(@PathVariable("travelNumber") Integer travelNumber, @RequestHeader("Authorization") String token) {

        Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();

        // 여행 번호와 사용자 번호로 북마크 삭제
        bookmarkService.removeBookmark(travelNumber, userNumber);
        return ResponseEntity.noContent().build();
    }

    // 사용자의 북마크 목록 조회
    @GetMapping
    public ResponseEntity<Page<BookmarkResponse>> getBookmarks(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size
            ) {
        Pageable pageable = PageRequest.of(page, size);
        Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();
        Page<BookmarkResponse> bookmarkResponses = bookmarkService.getBookmarksByUser(userNumber, page, size);
        return ResponseEntity.ok(bookmarkResponses);
    }

    //북마크한 travelNumber만 조회
    @GetMapping("/travel-number")
    public ResponseEntity<List<Integer>> getBookmarkedTravelNumbers(@RequestHeader("Authorization") String token) {

        Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();

        List<Integer> travelNumbers = bookmarkService.getBookmarkedTravelNumbers(userNumber);

        return ResponseEntity.ok(travelNumbers);
    }

}