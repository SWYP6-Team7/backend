package swyp.swyp6_team7.travel.util;

import swyp.swyp6_team7.travel.dto.TravelRecommendDto;

import java.util.Comparator;

public class TravelRecommendComparator implements Comparator<TravelRecommendDto> {

    @Override
    public int compare(TravelRecommendDto o1, TravelRecommendDto o2) {
        if (o1.getPreferredNumber() == o2.getPreferredNumber()) {
            return o1.getRegisterDue().compareTo(o2.getRegisterDue());
        }
        return -1 * (o1.getPreferredNumber() - o2.getPreferredNumber());
    }

}
