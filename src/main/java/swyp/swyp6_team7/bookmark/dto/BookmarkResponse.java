package swyp.swyp6_team7.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BookmarkResponse {
    private int travelNumber;           // 여행 번호
    private String title;               // 여행 제목
    private String location;
    private int userNumber;             // 사용자 번호
    private String userName;            // 사용자 이름
    private List<String> tags;          // 태그 리스트
    private int nowPerson;              // 현재 참가 인원 수
    private int maxPerson;              // 최대 참가 인원 수
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    private boolean isBookmarked;       // 북마크 여부

}
