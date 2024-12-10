package swyp.swyp6_team7.travel.dto.response;

import lombok.Getter;

@Getter
public class TravelCreateResponse {

    private int travelNumber;

    public TravelCreateResponse(int travelNumber) {
        this.travelNumber = travelNumber;
    }
}
