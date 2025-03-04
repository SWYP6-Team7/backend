package swyp.swyp6_team7.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_block_explanation")
// 신고 사유 테이블
public class UserBlockExplanation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "block_id", nullable = false)
    private Integer blockId;

    @Column(name = "block_explanation", nullable = false)
    private String blockExplanation;

    @Column(name = "is_replied", nullable = false)
    private Boolean isReplied;

    @Column(name = "reg_ts", nullable = false, updatable = false)
    private LocalDateTime regTs;

    @Column(name = "upd_ts", nullable = false)
    private LocalDateTime updTs;

    @Builder
    public UserBlockExplanation(
            Integer blockId,
            String blockExplanation,
            Boolean isReplied,
            Integer reportCount,
            LocalDateTime blockPeriod
    ) {
        this.id = 0;
        this.blockId = blockId;
        this.blockExplanation = blockExplanation;
        this.isReplied = isReplied;
        this.regTs = LocalDateTime.now();
        this.updTs = LocalDateTime.now();
    }
}
