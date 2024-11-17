package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.companion.service.CompanionService;
import swyp.swyp6_team7.travel.dto.response.TravelCompanionResponse;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TravelCompanionController {

    private final CompanionService companionService;

    @GetMapping("/api/travel/{travelNumber}/companions")
    public ResponseEntity<TravelCompanionResponse> getTravelCompanions(@PathVariable("travelNumber") int travelNumber) {

        List<CompanionInfoDto> companions = companionService.findCompanionsByTravelNumber(travelNumber);
        TravelCompanionResponse response = TravelCompanionResponse.from(companions);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

}
