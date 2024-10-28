package swyp.swyp6_team7.community.repository;

import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.dto.response.CommunitySearchDto;
import swyp.swyp6_team7.community.util.CommunitySearchSortingType;

import java.util.List;

public interface CommunityCustomRepository {

    void incrementViewCount(int postNumber);

    List<CommunitySearchDto> search(CommunitySearchCondition searchCondition);
    List<CommunitySearchDto> getMyList(CommunitySearchSortingType sortingType, int userNumber);

}
