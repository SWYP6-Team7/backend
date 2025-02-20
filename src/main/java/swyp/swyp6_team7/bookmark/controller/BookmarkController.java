package swyp.swyp6_team7.bookmark.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.bookmark.dto.BookmarkRequest;
import swyp.swyp6_team7.bookmark.dto.BookmarkResponse;
import swyp.swyp6_team7.bookmark.service.BookmarkService;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final JwtProvider jwtProvider;

    public BookmarkController(BookmarkService bookmarkService, JwtProvider jwtProvider) {
        this.bookmarkService = bookmarkService;
        this.jwtProvider = jwtProvider;
    }

    // 북마크 추가
    @PostMapping
    public ApiResponse<Void> addBookmark(
            @RequireUserNumber Integer userNumber,
            @RequestBody BookmarkRequest request
    ) {
        // userNumber를 요청에 추가
        request.setUserNumber(userNumber);
        bookmarkService.addBookmark(request);
        return ApiResponse.success(null);
    }

    // 북마크 삭제
    @DeleteMapping("/{travelNumber}")
    public ApiResponse<Void> removeBookmark(
            @PathVariable("travelNumber") Integer travelNumber,
            @RequireUserNumber Integer userNumber
    ) {
        // 여행 번호와 사용자 번호로 북마크 삭제
        bookmarkService.removeBookmark(travelNumber, userNumber);
        return ApiResponse.success(null);
    }

    // 사용자의 북마크 목록 조회
    @GetMapping
    public ApiResponse<Page<BookmarkResponse>> getBookmarks(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber
    ) {
        Page<BookmarkResponse> bookmarkResponses = bookmarkService.getBookmarksByUser(userNumber, page, size);
        return ApiResponse.success(bookmarkResponses);
    }

    //북마크한 travelNumber만 조회
    @GetMapping("/travel-number")
    public ApiResponse<List<Integer>> getBookmarkedTravelNumbers(
            @RequireUserNumber Integer userNumber
    ) {
        List<Integer> travelNumbers = bookmarkService.getBookmarkedTravelNumbers(userNumber);

        return ApiResponse.success(travelNumbers);
    }
}