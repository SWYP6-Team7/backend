package swyp.swyp6_team7.travel.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TravelEnrollmentLastViewedRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime lastViewedAt;

    @Builder
    public TravelEnrollmentLastViewedRequest(LocalDateTime lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }
}
