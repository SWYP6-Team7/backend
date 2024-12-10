package swyp.swyp6_team7.travel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import swyp.swyp6_team7.travel.dto.TravelDetailDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendForMemberDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendForNonMemberDto;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;

import java.time.LocalDate;
import java.util.List;

public interface TravelCustomRepository {

    TravelDetailDto getDetailsByNumber(int travelNumber);

    Page<TravelRecentDto> findAllSortedByCreatedAt(PageRequest pageRequest);

    Page<TravelRecommendForMemberDto> findAllByPreferredTags(PageRequest pageRequest, Integer loginUserNumber, List<String> preferredTags, LocalDate requestDate);

    public Page<TravelRecommendForNonMemberDto> findAllSortedByBookmarkNumberAndTitle(PageRequest pageRequest, LocalDate requestDate);

    Page<TravelSearchDto> search(TravelSearchCondition condition);

}
