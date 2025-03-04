package swyp.swyp6_team7.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDetailReason {
    private Integer reportReasonId;
    private String detailReason;

    public ReportDetailReason(Integer id, String reason) {
        this.reportReasonId = id;
        this.detailReason = reason;
    }
}
