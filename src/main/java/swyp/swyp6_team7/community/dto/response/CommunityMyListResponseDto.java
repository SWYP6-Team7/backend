package swyp.swyp6_team7.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.community.domain.Community;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CommunityMyListResponseDto {

    private int postNumber;         //게시글 번호
    private int userNumber;         //게시글 작성자 유저 번호
    private String postWriter;      //게시글 작성자명

    private int categoryNumber;     //카테고리 번호
    private String categoryName;    //카테고리명

    private String title;           //게시글 제목
    private String content;         //게시글 내용
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime regDate;         //게시글 등록 일시

    private long commentCount;       //해당 게시 글의 댓글 수
    private int viewCount;          //해당 게시글의 조회수

    private long likeCount;          //해당 게시글의 좋아요 수
    private boolean liked;          //해당 게시글에 좋아요를 눌렀는지 여부

    private String thumbnailUrl; //게시글 썸네일 이미지 url
    private long totalCount;    //몇건인지


    public CommunityMyListResponseDto (
            int postNumber, int userNumber, String postWriter,
            int categoryNumber, String categoryName,
            String title, String content, LocalDateTime regDate,
            long commentCount, int viewCount, long likeCount, boolean liked, String thumbnailUrl, long totalCount
    ) {
        this.postNumber = postNumber;
        this.userNumber = userNumber;
        this.postWriter = postWriter;

        this.categoryNumber = categoryNumber;
        this.categoryName = categoryName;

        this.title = title;
        this.content = content;
        this.regDate = regDate;

        this.commentCount = commentCount;
        this.viewCount = viewCount;

        this.likeCount = likeCount;
        this.liked = liked;
        this.thumbnailUrl = thumbnailUrl;
        this.totalCount = totalCount;
    }

    public static CommunityMyListResponseDto fromEntity(Community community, String postWriter, String categoryName, long commentCount, long likeCount, boolean liked, String thumbnailUrl, long totalCount) {
        return new CommunityMyListResponseDto (
                community.getPostNumber(),
                community.getUserNumber(),
                postWriter,
                community.getCategoryNumber(),
                categoryName,
                community.getTitle(),
                community.getContent(),
                community.getRegDate(),
                commentCount,
                community.getViewCount(),
                likeCount,
                liked,
                thumbnailUrl,
                totalCount
        );
    }
}
