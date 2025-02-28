package swyp.swyp6_team7.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_block")
// 신고 사유 테이블
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_number", nullable = false)
    private int userNumber; // 정지된 사람

    @Column(name = "active", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false)
    private BlockType blockType;

    @Column(name = "block_period")
    private LocalDate blockPeriod;

    @Column(name = "reg_ts", nullable = false, updatable = false)
    private LocalDateTime regTs;

    @Column(name = "upd_ts", nullable = false)
    private LocalDateTime updTs;

    @Builder
    public UserBlock(
            Integer userNumber,
            BlockType blockType,
            Boolean isActive,
            LocalDate blockPeriod
    ) {
        this.id = 0;
        this.userNumber = userNumber;
        this.isActive = isActive;
        this.blockType = blockType;
        this.blockPeriod = blockPeriod;
        this.regTs = LocalDateTime.now();
        this.updTs = LocalDateTime.now();
    }

    public boolean isValidBlock() {
        return isActive && blockPeriod.isAfter(LocalDate.now());
    }
}
