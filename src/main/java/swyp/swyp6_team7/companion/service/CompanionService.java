package swyp.swyp6_team7.companion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.companion.dto.CompanionInfoDto;
import swyp.swyp6_team7.companion.repository.CompanionRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanionService {

    private final CompanionRepository companionRepository;


    public List<CompanionInfoDto> findCompanionsByTravelNumber(int travelNumber) {
        List<CompanionInfoDto> companions = companionRepository.findCompanionInfoByTravelNumber(travelNumber);
        return companions;
    }

}
