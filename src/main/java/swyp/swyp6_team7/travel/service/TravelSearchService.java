package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.service.BookmarkService;
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
        Page<TravelSearchDto> result = travelRepository.search(condition);

        // 로그인 사용자 좋아요(북마크여부) 처리
        if (loginUserNumber != null) {
            List<Integer> travelsNumber = result.getContent().stream()
                    .map(travelRecentDto -> travelRecentDto.getTravelNumber())
                    .toList();

            Map<Integer, Boolean> bookmarkedMap = bookmarkService.getBookmarkExistenceByTravelNumbers(loginUserNumber, travelsNumber);

            // 각 여행에 대해 북마크 여부 설정
            for (TravelSearchDto travelRecentDto : result.getContent()) {
                Boolean bookmarked = bookmarkedMap.get(travelRecentDto.getTravelNumber());
                travelRecentDto.updateBookmarked(bookmarked);
            }
        }

        return result;
    }

}
