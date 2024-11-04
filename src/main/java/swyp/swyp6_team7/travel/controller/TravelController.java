package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;
import swyp.swyp6_team7.travel.service.TravelService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TravelController {

    private static final Logger logger = LoggerFactory.getLogger(TravelController.class);
    private final TravelService travelService;

    @PostMapping("/api/travel")
    public ResponseEntity<TravelDetailResponse> create(@RequestBody @Validated TravelCreateRequest request) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        logger.info("Travel 생성 요청 - userId: {}", loginUserNumber);

        Travel createdTravel = travelService.create(request, loginUserNumber);
        logger.info("Travel 생성 완료 - createdTravel: {}", createdTravel);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(travelService.getDetailsByNumber(createdTravel.getNumber()));
    }

    @GetMapping("/api/travel/detail/{travelNumber}")
    public ResponseEntity<TravelDetailResponse> getDetailsByNumber(
            @PathVariable("travelNumber") int travelNumber
    ) {
        TravelDetailResponse travelDetails = travelService.getDetailsByNumber(travelNumber);
        travelService.addViewCount(travelNumber); //조회수 update

        return ResponseEntity.status(HttpStatus.OK)
                .body(travelDetails);
    }

    @PutMapping("/api/travel/{travelNumber}")
    public ResponseEntity<TravelDetailResponse> update(
            @PathVariable("travelNumber") int travelNumber,
            @RequestBody TravelUpdateRequest request
    ) {
        travelService.update(travelNumber, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(travelService.getDetailsByNumber(travelNumber));
    }

    @DeleteMapping("/api/travel/{travelNumber}")
    public ResponseEntity delete(@PathVariable("travelNumber") int travelNumber) {
        travelService.delete(travelNumber);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/api/travels/search")
    public ResponseEntity<Page<TravelSearchDto>> search(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "location", required = false) List<String> selectedLocation,
            @RequestParam(name = "gender", required = false) List<String> selectedGender,
            @RequestParam(name = "person", required = false) List<String> selectedPerson,
            @RequestParam(name = "period", required = false) List<String> selectedPeriod,
            @RequestParam(name = "tags", required = false) List<String> selectedTags,
            @RequestParam(name = "sorting", required = false) String selectedSortingType
    ) {

        TravelSearchCondition condition = TravelSearchCondition.builder()
                .pageRequest(PageRequest.of(page, size))
                .keyword(keyword)
                .locationTypes(selectedLocation)
                .genderTypes(selectedGender)
                .personTypes(selectedPerson)
                .periodTypes(selectedPeriod)
                .tags(selectedTags)
                .sortingType(selectedSortingType)
                .build();

        Page<TravelSearchDto> travels = travelService.search(condition);
        return ResponseEntity.status(HttpStatus.OK)
                .body(travels);
    }

}
