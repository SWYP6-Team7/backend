package swyp.swyp6_team7.community.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.community.domain.Community;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityCreateRequestDto {

    private String categoryName;
    private String title;
    private String content;

    public CommunityCreateRequestDto(String categoryName, String title, String content) {
        this.categoryName = categoryName;
        this.title = title;
        this.content = content;
    }

    public Community toCommunityEntity(int userNumber, int categoryNumber, String title, String content, LocalDateTime regDate, int viewCount) {
        return Community.builder()
                .userNumber(userNumber)
                .categoryNumber(categoryNumber)
                .title(title)
                .content(content)
                .regDate(LocalDateTime.now())
                .viewCount(0)
                .build();
    }

    @Override
    public String toString() {
        return "CommunityCreateRequestDto(categoryName=" + categoryName + ", title=" + title + ", content=" + content + ")";
    }
}
