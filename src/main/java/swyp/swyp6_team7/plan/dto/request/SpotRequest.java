package swyp.swyp6_team7.plan.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpotRequest {
    @NotBlank
    private String name;
    private String category;
    private String region;
    @NotBlank
    private String latitude;
    @NotBlank
    private String longitude;
}
