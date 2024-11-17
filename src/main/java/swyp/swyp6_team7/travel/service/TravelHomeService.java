package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.tag.repository.UserTagPreferenceRepository;
import swyp.swyp6_team7.travel.dto.TravelRecommendDto;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;
import swyp.swyp6_team7.travel.util.TravelRecommendComparator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TravelHomeService {

    private final TravelRepository travelRepository;
    private final UserTagPreferenceRepository userTagPreferenceRepository;

    public Page<TravelRecentDto> getTravelsSortedByCreatedAt(PageRequest pageRequest, Integer loginUserNumber) {
        return travelRepository.findAllSortedByCreatedAt(pageRequest, loginUserNumber);
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
