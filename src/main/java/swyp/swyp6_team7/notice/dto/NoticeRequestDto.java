package swyp.swyp6_team7.notice.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoticeRequestDto {
    private String title;
    private String content;

    // 생성자
    public NoticeRequestDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
