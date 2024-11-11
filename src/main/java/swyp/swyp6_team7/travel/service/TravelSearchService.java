package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;
import swyp.swyp6_team7.travel.repository.TravelRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TravelSearchService {

    private final TravelRepository travelRepository;

    public Page<TravelSearchDto> search(TravelSearchCondition condition, Integer loginUserNumber) {
        Page<TravelSearchDto> result = travelRepository.search(condition, loginUserNumber);
        return result;
    }

}
