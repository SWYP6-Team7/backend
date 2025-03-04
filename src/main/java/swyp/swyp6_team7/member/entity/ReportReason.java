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
@Table(name = "report_reason")
// 신고 사유 테이블
public class ReportReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_category", nullable = false)
    private ReportCategory reportCategory; // 신고 유형

    @Column(name = "reason")
    private String reason;

    @Column(name = "reg_ts", nullable = false, updatable = false)
    private LocalDateTime regTs;

    @Column(name = "upd_ts", nullable = false)
    private LocalDateTime updTs;

    @Builder
    public ReportReason(ReportCategory reportCategory, String reason) {
        this.id = 0;
        this.reportCategory = reportCategory;
        this.reason = reason;
        this.regTs = LocalDateTime.now();
        this.updTs = LocalDateTime.now();
    }
}
