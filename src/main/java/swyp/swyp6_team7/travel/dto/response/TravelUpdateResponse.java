package swyp.swyp6_team7.travel.dto.response;

import lombok.Getter;

@Getter
public class TravelUpdateResponse {

    private int travelNumber;

    public TravelUpdateResponse(int travelNumber) {
        this.travelNumber = travelNumber;
    }
}
