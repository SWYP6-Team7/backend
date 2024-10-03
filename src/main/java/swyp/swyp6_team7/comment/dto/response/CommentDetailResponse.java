package swyp.swyp6_team7.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.swyp6_team7.comment.domain.Comment;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CommentDetailResponse {

    private int commentNumber;
    private int userNumber;
    private String content;
    private int parentNumber;
    private int likes;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime regDate;
    private String relatedType;
    private int relatedNumber;

    @Builder
    public CommentDetailResponse(
            int commentNumber, int userNumber, String content, int parentNumber,
            int likes, LocalDateTime regDate, String relatedType, int relatedNumber
    ) {
        this.commentNumber = commentNumber;
        this.userNumber = userNumber;
        this.content = content;
        this.parentNumber = parentNumber;
        this.likes = likes;
        this.regDate = regDate;
        this.relatedType = relatedType;
        this.relatedNumber = relatedNumber;
    }

    public CommentDetailResponse(Comment comment) {
        this.commentNumber = comment.getCommentNumber();
        this.userNumber = comment.getUserNumber();
        this.content = comment.getContent();
        this.parentNumber = comment.getParentNumber();
        this.likes = comment.getLikes();
        this.regDate = comment.getRegDate();
        this.relatedType = comment.getRelatedType();
        this.relatedNumber = comment.getRelatedNumber();
    }

    @Override
    public String toString() {
        return "CommentDetailResponse{" +
                "commentNumber=" + commentNumber +
                ", userNumber=" + userNumber +
                ", content='" + content + '\'' +
                ", parentNumber=" + parentNumber +
                ", likes=" + likes +
                ", regDate=" + regDate +
                ", relatedType='" + relatedType + '\'' +
                ", relatedNumber=" + relatedNumber +
                '}';
    }
}
