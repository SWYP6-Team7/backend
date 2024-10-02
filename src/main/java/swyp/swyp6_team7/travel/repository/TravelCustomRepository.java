package swyp.swyp6_team7.travel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.travel.dto.TravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendDto;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;

import java.util.List;

public interface TravelCustomRepository {

    Page<TravelRecentDto> findAllSortedByCreatedAt(PageRequest pageRequest);

    Page<TravelRecommendDto> findAllByPreferredTags(PageRequest pageRequest, List<String> preferredTags);

    Page<TravelSearchDto> search(TravelSearchCondition condition);

    TravelDetailDto getDetailsByNumber(int travelNumber);

}
