package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.service.BookmarkService;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TravelSearchService {

    private final TravelRepository travelRepository;
    private final BookmarkService bookmarkService;

    public Page<TravelSearchDto> search(TravelSearchCondition condition, Integer loginUserNumber) {
        try {
            Page<TravelSearchDto> result = travelRepository.search(condition);

            // 사용자 북마크 설정
            if (loginUserNumber != null) {
                setUserBookmarkedToSearchResult(loginUserNumber, result);
            }
            return result;

        } catch (Exception e) {
            log.warn("여행 검색 중 오류 발생: {}", e.getMessage());
            throw new MoingApplicationException("여행 검색 도중 오류가 발생했습니다.");
        }
    }

    // 로그인 사용자 북마크 정보 추가 메서드
    private void setUserBookmarkedToSearchResult(Integer userNumber, Page<TravelSearchDto> searchResult) {
        List<Integer> travelsNumber = searchResult.getContent().stream()
                .map(travelRecentDto -> travelRecentDto.getTravelNumber())
                .toList();

        Map<Integer, Boolean> bookmarkedMap = bookmarkService.getBookmarkExistenceByTravelNumbers(userNumber, travelsNumber);

        // 각 여행에 대해 북마크 여부 설정
        for (TravelSearchDto travelSearchDto : searchResult.getContent()) {
            Boolean bookmarked = bookmarkedMap.get(travelSearchDto.getTravelNumber());
            travelSearchDto.updateBookmarked(bookmarked);
        }
    }
}
