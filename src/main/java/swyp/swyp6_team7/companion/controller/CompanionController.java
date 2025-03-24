package swyp.swyp6_team7.companion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.companion.service.CompanionService;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.travel.dto.response.TravelCompanionResponse;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CompanionController {

    private final CompanionService companionService;

    // 여행 참가자 목록 조회
    @GetMapping("/api/travel/{travelNumber}/companions")
    public ApiResponse<TravelCompanionResponse> getTravelCompanions(@PathVariable("travelNumber") int travelNumber) {
        List<CompanionInfoDto> companions = companionService.findCompanionsByTravelNumber(travelNumber);
        TravelCompanionResponse response = TravelCompanionResponse.from(companions);
        return ApiResponse.success(response);
    }
}
