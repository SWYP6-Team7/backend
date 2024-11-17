package swyp.swyp6_team7.companion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.companion.repository.CompanionRepository;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanionService {

    private final CompanionRepository companionRepository;
    private final TravelRepository travelRepository;

    public List<CompanionInfoDto> findCompanionsByTravelNumber(int travelNumber) {

        if (!travelRepository.existsTravelByNumber(travelNumber)) {
            log.warn("Companion find - 존재하지 않는 여행 콘텐츠입니다. travelNumber: {}", travelNumber);
            throw new IllegalArgumentException("존재하지 않는 여행 콘텐츠입니다.");
        }

        List<CompanionInfoDto> companions = companionRepository.findCompanionInfoByTravelNumber(travelNumber);
        return companions;
    }

}
