package swyp.swyp6_team7.travel.util;

import swyp.swyp6_team7.travel.dto.TravelRecommendForMemberDto;

import java.util.Comparator;

public class TravelRecommendComparator implements Comparator<TravelRecommendForMemberDto> {

    @Override
    public int compare(TravelRecommendForMemberDto o1, TravelRecommendForMemberDto o2) {
        if (o1.getPreferredNumber() == o2.getPreferredNumber()) {
            if (o1.getRegisterDue().compareTo(o2.getRegisterDue()) == 0) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
            return o1.getRegisterDue().compareTo(o2.getRegisterDue());
        }
        return -1 * (o1.getPreferredNumber() - o2.getPreferredNumber());
    }

}
