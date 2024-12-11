package swyp.swyp6_team7.community.dto.response;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.community.domain.Community;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CommunityDetailResponseDto {

    private int postNumber;         //게시글 번호
    private int userNumber;         //게시글 작성자 유저 번호
    private String postWriter;      //게시글 작성자명

    private Integer categoryNumber;     //카테고리 번호
    private String categoryName;    //카테고리명

    private String title;           //게시글 제목
    private String content;         //게시글 내용
    private String regDate;         //게시글 등록 일시

    private long commentCount;       //해당 게시글의 댓글 수
    private int viewCount;          //해당 게시글의 조회수

    private long likeCount;          //해당 게시글의 좋아요 수
    private boolean liked;          //해당 게시글에 좋아요를 눌렀는지 여부

    private String profileImageUrl; //게시글 작성자의 프로필 이미지

    //날짜 포맷 변경
    public static String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"));
    }

    @Builder
    public CommunityDetailResponseDto(
            int postNumber, int userNumber, String postWriter,
            int categoryNumber, String categoryName,
            String title, String content, LocalDateTime regDate,
            long commentCount, int viewCount, long likeCount, boolean liked, String profileImageUrl
    ) {
        this.postNumber = postNumber;
        this.userNumber = userNumber;
        this.postWriter = postWriter;

        this.categoryNumber = categoryNumber;
        this.categoryName = categoryName;

        this.title = title;
        this.content = content;
        this.regDate = formatDate(regDate);

        this.commentCount = commentCount;
        this.viewCount = viewCount;

        this.likeCount = likeCount;
        this.liked = liked;

        this.profileImageUrl = profileImageUrl;
    }

    public static CommunityDetailResponseDto fromEntity(Community community, String postWriter, String categoryName, long commentCount, long likeCount, boolean liked, String profileImageUrl) {
        return new CommunityDetailResponseDto(
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
                profileImageUrl
                );
    }
}

