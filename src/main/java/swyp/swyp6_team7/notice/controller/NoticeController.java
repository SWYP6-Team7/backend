package swyp.swyp6_team7.notice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.notice.dto.NoticeRequestDto;
import swyp.swyp6_team7.notice.dto.NoticeResponseDto;
import swyp.swyp6_team7.notice.entity.Notice;
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
    public ResponseEntity<List<NoticeResponseDto>> getAllNotices() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> getNoticeById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(noticeService.getNoticeById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<NoticeResponseDto> createNotice(@RequestBody NoticeRequestDto noticeRequestDto) {
        return ResponseEntity.status(201).body(noticeService.createNotice(noticeRequestDto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> updateNotice(
            @PathVariable("id") Long id, @RequestBody NoticeRequestDto noticeRequestDto) {
        return ResponseEntity.ok(noticeService.updateNotice(id, noticeRequestDto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
