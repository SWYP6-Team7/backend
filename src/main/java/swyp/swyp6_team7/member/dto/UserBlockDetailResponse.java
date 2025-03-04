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
    private boolean isBlocked;
    @Nullable
    private String reason;
    @Nullable
    private LocalDate blockPeriod;
}
