package swyp.swyp6_team7.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import swyp.swyp6_team7.comment.domain.Comment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Getter
@AllArgsConstructor
public class CommentListReponseDto {


    private int commentNumber;      //댓글 번호
    private int userNumber;         //회원 번호
    private String content;         //댓글 내용
    private int parentNumber;       //부모 댓글 번호
    private String regDate;         //등록 일시
    private String relatedType;     //어느 게시물?
    private int relatedNumber;      //게시물 번호
    private String writer;          // 댓글 작성자
    private long repliesCount;      // 답글 수
    private long likes;             //좋아요 수
    private boolean liked;          // 좋아요 눌렀는지 여부
    private int travelWriterNumber; //게시글의 작성자 회원 번호
    private String imageUrl;        //댓글 작성자의 프로필 이미지

    private Boolean commented;      //답글 작성 여부


    public static String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"));
    }



    public static CommentListReponseDto fromEntity(Comment comment, String writer, long repliesCount, long likes, boolean liked, int travelWriterNumber, String imageUrl, Boolean commented) {
        return new CommentListReponseDto(
                comment.getCommentNumber(),
                comment.getUserNumber(),
                comment.getContent(),
                comment.getParentNumber(),
                formatDate(comment.getRegDate()),
                comment.getRelatedType(),
                comment.getRelatedNumber(),
                writer,
                repliesCount,
                likes,
                liked,
                travelWriterNumber
                ,imageUrl
                ,commented
        );
    }

    @Override
    public String toString() {
        return "CommentListResponseDto{" +
                "commentNumber=" + commentNumber +
                ", userNumber=" + userNumber +
                ", content='" + content + '\'' +
                ", parentNumber=" + parentNumber +
                ", regDate='" + regDate + '\'' +
                ", relatedType='" + relatedType + '\'' +
                ", relatedNumber=" + relatedNumber +
                ", writer='" + writer + '\'' +
                ", repliesCount=" + repliesCount +
                ", likes=" + likes +
                ", liked=" + liked + '\'' +
                ", travelWriterNumber=" + travelWriterNumber +
                ", imageUrl='" + imageUrl + '\'' +
                "commented=" + commented +
                '}';
    }

}