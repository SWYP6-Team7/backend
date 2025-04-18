package swyp.swyp6_team7.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserReportStats {
    private boolean recentlyReported; // 최근 7일 내 신고당함 여부
    private int totalReportCount; // 누적 신고 당한 횟수
    private int recentReportCount; // 최근 7내 신고당한 횟수
}
