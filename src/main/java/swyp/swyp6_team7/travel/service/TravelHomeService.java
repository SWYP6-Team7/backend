package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.bookmark.service.BookmarkService;
import swyp.swyp6_team7.tag.repository.UserTagPreferenceRepository;
import swyp.swyp6_team7.travel.dto.TravelRecommendDto;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;
import swyp.swyp6_team7.travel.util.TravelRecommendComparator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TravelHomeService {

    private final TravelRepository travelRepository;
    private final UserTagPreferenceRepository userTagPreferenceRepository;
    private final BookmarkService bookmarkService;

    public Page<TravelRecentDto> getTravelsSortedByCreatedAt(PageRequest pageRequest, Integer loginUserNumber) {
        Page<TravelRecentDto> result = travelRepository.findAllSortedByCreatedAt(pageRequest);

        // 로그인 사용자 좋아요(북마크여부) 처리
        if (loginUserNumber != null) {
            List<Integer> travelsNumber = result.getContent().stream()
                    .map(travelRecentDto -> travelRecentDto.getTravelNumber())
                    .toList();

            Map<Integer, Boolean> bookmarkedMap = bookmarkService.getBookmarkExistenceByTravelNumbers(loginUserNumber, travelsNumber);

            // 각 여행에 대해 북마크 여부 설정
            for (TravelRecentDto travelRecentDto : result.getContent()) {
                Boolean bookmarked = bookmarkedMap.get(travelRecentDto.getTravelNumber());
                travelRecentDto.updateBookmarked(bookmarked);
            }
        }

        return result;
    }

    public Page<TravelRecommendDto> getRecommendTravelsByUser(PageRequest pageRequest, Integer loginUserNumber, LocalDate requestDate) {

        List<String> preferredTags = userTagPreferenceRepository.findPreferenceTagsByUserNumber(loginUserNumber);
        log.info("TravelHomeService recommend - userId: {}, preferredTags: {}", loginUserNumber, preferredTags);

        Page<TravelRecommendDto> result = travelRepository.findAllByPreferredTags(pageRequest, loginUserNumber, preferredTags, requestDate);

        List<TravelRecommendDto> travels = new ArrayList<>(result.getContent());
        Collections.sort(travels, new TravelRecommendComparator());

        return new PageImpl(travels, pageRequest, result.getTotalElements());
    }
}
