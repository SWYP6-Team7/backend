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
@Table(name = "user_block_report")
// 유저 신고 테이블
public class UserBlockReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reporter_user_number", nullable = false)
    private Integer reporterUserNumber; // 신고한 사람

    @Column(name = "reported_user_number", nullable = false)
    private Integer reportedUserNumber; // 신고된 사람

    @Column(name = "report_reason_id", nullable = false)
    private Integer reportReasonId; // 신고 유형 ID

    @Column(name = "report_reason_extra")
    private String reportReasonExtra; // 신고 사유 (기타 사유 입력내용)

    // TODO: 신고 유효 여부도 추가

    @Column(name = "reg_ts", nullable = false, updatable = false)
    private LocalDateTime regTs;

    @Column(name = "upd_ts", nullable = false)
    private LocalDateTime updTs;

    @Builder
    public UserBlockReport(
            Integer reporterUserNumber,
            Integer reportedUserNumber,
            Integer reportReasonId,
            String reportReasonExtra
    ) {
        this.id = 0;
        this.reporterUserNumber = reporterUserNumber;
        this.reportedUserNumber = reportedUserNumber;
        this.reportReasonId = reportReasonId;
        this.reportReasonExtra = reportReasonExtra;
        this.regTs = LocalDateTime.now();
        this.updTs = LocalDateTime.now();
    }
}
