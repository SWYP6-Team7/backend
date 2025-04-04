package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.travel.dto.TravelSearchCondition;
import swyp.swyp6_team7.travel.dto.response.TravelSearchDto;
import swyp.swyp6_team7.travel.service.TravelSearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TravelSearchController {

    private final TravelSearchService travelSearchService;

    // 여행 검색
    @GetMapping("/api/travels/search")
    public ApiResponse<Page<TravelSearchDto>> search(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "location", required = false) List<String> selectedLocation,
            @RequestParam(name = "gender", required = false) List<String> selectedGender,
            @RequestParam(name = "person", required = false) List<String> selectedPerson,
            @RequestParam(name = "period", required = false) List<String> selectedPeriod,
            @RequestParam(name = "tags", required = false) List<String> selectedTags,
            @RequestParam(name = "sorting", required = false) String selectedSortingType,
            @RequireUserNumber Integer userNumber
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

        Page<TravelSearchDto> travels = travelSearchService.search(condition, userNumber);
        return ApiResponse.success(travels);
    }

}
