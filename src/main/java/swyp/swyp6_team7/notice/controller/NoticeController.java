package swyp.swyp6_team7.notice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.notice.dto.NoticeRequestDto;
import swyp.swyp6_team7.notice.dto.NoticeResponseDto;
import swyp.swyp6_team7.notice.service.NoticeService;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<List<NoticeResponseDto>> getAllNotices() {
        return ApiResponse.success(noticeService.getAllNotices());
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticeResponseDto> getNoticeById(@PathVariable("id") Long id) {
        return ApiResponse.success(noticeService.getNoticeById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ApiResponse<NoticeResponseDto> createNotice(@RequestBody NoticeRequestDto noticeRequestDto) {
        return ApiResponse.success(noticeService.createNotice(noticeRequestDto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<NoticeResponseDto> updateNotice(
            @PathVariable("id") Long id, @RequestBody NoticeRequestDto noticeRequestDto) {
        return ApiResponse.success(noticeService.updateNotice(id, noticeRequestDto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return ApiResponse.success(null);
    }
}
