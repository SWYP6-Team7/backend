package swyp.swyp6_team7.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReportReasonResponse {
    private String reportCategoryName;
    private List<ReportDetailReason> reportDetailReasons;

    public ReportReasonResponse(String value, List<ReportDetailReason> reasons) {
        this.reportCategoryName = value;
        this.reportDetailReasons = reasons;
    }
}
