package swyp.swyp6_team7.member.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserReportRequest {
    private int reportedUserNumber;
    private int reportReasonId;
    @Nullable
    private String reportReasonExtra;
}
