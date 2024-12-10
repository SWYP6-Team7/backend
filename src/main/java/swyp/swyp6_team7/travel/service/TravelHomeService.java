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
import swyp.swyp6_team7.travel.dto.TravelRecommendForMemberDto;
import swyp.swyp6_team7.travel.dto.TravelRecommendForNonMemberDto;
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

    public Page<TravelRecommendForMemberDto> getRecommendTravelsByMember(PageRequest pageRequest, Integer loginUserNumber, LocalDate requestDate) {
        // 사용자 선호 태그
        List<String> preferredTags = userTagPreferenceRepository.findPreferenceTagsByUserNumber(loginUserNumber);

        try {
            // 조회
            Page<TravelRecommendForMemberDto> result = travelRepository.findAllByPreferredTags(pageRequest, loginUserNumber, preferredTags, requestDate);

            // 태그 매칭 개수 기반 정렬
            List<TravelRecommendForMemberDto> travels = new ArrayList<>(result.getContent());
            Collections.sort(travels, new TravelRecommendComparator());

            return new PageImpl(travels, pageRequest, result.getTotalElements());
        } catch (Exception e) {
            log.warn("로그인 사용자 추천 여행 목록 조회 실패: {}", e);
            throw new IllegalArgumentException("로그인 사용자 추천 여행 목록 조회 실패");
        }
    }

    public Page<TravelRecommendForNonMemberDto> getRecommendTravelsByNonMember(PageRequest pageRequest, LocalDate requestDate) {
        // 조회: 북마크 개수 많은 순서, 제목 사전순 정렬
        try {
            Page<TravelRecommendForNonMemberDto> result = travelRepository.findAllSortedByBookmarkNumberAndTitle(pageRequest, requestDate);
            return result;
        } catch (Exception e) {
            log.warn("비로그인 사용자 추천 여행 목록 조회 실패: {}", e);
            throw new IllegalArgumentException("비로그인 사용자 추천 여행 목록 조회 실패");
        }
    }

}
