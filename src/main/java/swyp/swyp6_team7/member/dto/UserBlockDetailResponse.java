package swyp.swyp6_team7.member.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class UserBlockDetailResponse {
    private Integer userNumber;
    private String userEmail;
    private String userName;
    private boolean isBlocked;
    @Nullable
    private String reason; // 현재는 사유 없어서 null
    @Nullable
    private LocalDate blockPeriod;
}
