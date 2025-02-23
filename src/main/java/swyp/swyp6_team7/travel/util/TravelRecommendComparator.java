package swyp.swyp6_team7.travel.util;

import swyp.swyp6_team7.travel.dto.TravelRecommendForMemberDto;

import java.util.Comparator;

public class TravelRecommendComparator implements Comparator<TravelRecommendForMemberDto> {

    @Override
    public int compare(TravelRecommendForMemberDto o1, TravelRecommendForMemberDto o2) {
        if (o1.getPreferredNumber() == o2.getPreferredNumber()) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
        return -1 * (o1.getPreferredNumber() - o2.getPreferredNumber());
    }

}
