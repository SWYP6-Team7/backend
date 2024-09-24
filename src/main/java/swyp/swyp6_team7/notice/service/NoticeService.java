package swyp.swyp6_team7.notice.service;

import org.springframework.stereotype.Service;
import swyp.swyp6_team7.notice.dto.NoticeRequestDto;
import swyp.swyp6_team7.notice.dto.NoticeResponseDto;
import swyp.swyp6_team7.notice.entity.Notice;
import swyp.swyp6_team7.notice.repository.NoticeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    // 전체 공지사항 조회
    public List<NoticeResponseDto> getAllNotices() {
        // Notice 엔티티를 NoticeResponseDto로 변환하여 반환
        return noticeRepository.findAll().stream()
                .map(notice -> new NoticeResponseDto(
                        notice.getNoticeId(),
                        notice.getTitle(),
                        notice.getContent(),
                        notice.getCreatedDate(),
                        notice.getViewCount()
                ))
                .collect(Collectors.toList()); // 변환된 리스트를 수집하여 반환
    }

    // ID로 공지사항 조회
    public NoticeResponseDto getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found"));
        return new NoticeResponseDto(notice.getNoticeId(), notice.getTitle(), notice.getContent(), notice.getCreatedDate(), notice.getViewCount());
    }

    // 공지사항 생성
    public NoticeResponseDto createNotice(NoticeRequestDto noticeRequestDto) {
        // 새로운 Notice 엔티티 생성 후 값 설정
        Notice notice = new Notice();
        notice.setTitle(noticeRequestDto.getTitle());
        notice.setContent(noticeRequestDto.getContent());
        notice.setCreatedDate(LocalDateTime.now()); // 생성일 현재 시간으로 설정

        // Notice 엔티티를 저장
        Notice savedNotice = noticeRepository.save(notice);

        // 저장된 공지사항을 NoticeResponseDto로 변환하여 반환
        return new NoticeResponseDto(
                savedNotice.getNoticeId(),
                savedNotice.getTitle(),
                savedNotice.getContent(),
                savedNotice.getCreatedDate(),
                savedNotice.getViewCount()
        );
    }


    // 공지사항 수정
    public NoticeResponseDto updateNotice(Long id, NoticeRequestDto noticeRequestDto) {
        // 수정할 공지사항을 ID로 조회
        Notice existingNotice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found"));

        // 제목과 내용을 요청된 값으로 업데이트
        existingNotice.setTitle(noticeRequestDto.getTitle());
        existingNotice.setContent(noticeRequestDto.getContent());

        // 업데이트된 공지사항 저장
        Notice updatedNotice = noticeRepository.save(existingNotice);

        // 수정된 공지사항을 NoticeResponseDto로 변환하여 반환
        return new NoticeResponseDto(
                updatedNotice.getNoticeId(),
                updatedNotice.getTitle(),
                updatedNotice.getContent(),
                updatedNotice.getCreatedDate(),
                updatedNotice.getViewCount()
        );
    }


    // 공지사항 삭제
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}
