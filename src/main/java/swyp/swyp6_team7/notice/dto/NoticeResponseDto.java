package swyp.swyp6_team7.notice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NoticeResponseDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private int viewCount;

    // 생성자
    public NoticeResponseDto(Long id, String title, String content, LocalDateTime createdDate, int viewCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.viewCount = viewCount;
    }
}
