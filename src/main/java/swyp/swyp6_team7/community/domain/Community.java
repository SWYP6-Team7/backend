package swyp.swyp6_team7.community.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Table(name = "Community_Posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_post_number", updatable = false, nullable = false)
    private int postNumber;

    //작성자 식별자
    @Column(name = "user_number", nullable = false)
    private int userNumber;

    //카테고리 식별자
    @Column(name = "community_category_number")
    private Integer categoryNumber;

    @Column(name = "community_post_title", length= 20, nullable = false)
    @Size(max = 20)
    private String title;

    @Column(name = "community_post_content", length = 2000, nullable = false)
    @Size(max = 2000)
    private String content;

    @Column(name = "community_post_reg_date", nullable = false, updatable = false)
    private LocalDateTime regDate;

    @Column(name = "community_post_view_count", nullable = false)
    private int viewCount;

    //C
    @Builder
    public Community (int userNumber, int categoryNumber, String title, String content, LocalDateTime regDate, int viewCount) {
        this.userNumber = userNumber;
        this.categoryNumber = categoryNumber;
        this.title = title;
        this.content = content;
        this.regDate = regDate;
        this.viewCount = viewCount;

    }

    //U
    public Community update (int categoryNumber, String title, String content) {
        this.categoryNumber = categoryNumber;
        this.title = title;
        this.content = content;
    return this;
    }

    //D
    public Community delete (int postNumber) {
        this.postNumber = postNumber;
        return this;
    }



}
