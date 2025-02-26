package swyp.swyp6_team7.member.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserBlockDetailResponse {
    private Integer userNumber;
    private boolean isBlocked;
    @Nullable
    private String reason;
    @Nullable
    private LocalDateTime blockPeriod;
}
