package swyp.swyp6_team7.companion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.companion.repository.CompanionRepository;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
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
            log.warn("존재하지 않는 여행 참가자 조회 요청: travelNumber={}", travelNumber);
            throw new MoingApplicationException("해당 여행은 존재하지 않습니다. travelNumber=" + travelNumber);
        }

        return companionRepository.findCompanionInfoByTravelNumber(travelNumber);
    }

}
