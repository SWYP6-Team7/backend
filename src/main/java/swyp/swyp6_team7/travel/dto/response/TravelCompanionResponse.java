package swyp.swyp6_team7.travel.dto.response;

import lombok.Builder;
import lombok.Getter;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;

import java.util.List;

@Getter
public class TravelCompanionResponse {

    private long totalCount;
    private List<CompanionInfoDto> companions;

    @Builder
    public TravelCompanionResponse(long totalCount, List<CompanionInfoDto> companions) {
        this.totalCount = totalCount;
        this.companions = companions;
    }

    public static TravelCompanionResponse from(List<CompanionInfoDto> companions) {
        return TravelCompanionResponse.builder()
                .totalCount(companions.size())
                .companions(companions)
                .build();
    }

}
