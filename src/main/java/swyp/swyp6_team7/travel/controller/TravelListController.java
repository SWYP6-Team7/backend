package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelListService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-travels")
public class TravelListController {
    private final TravelListService travelListService;


    @GetMapping("")
    public ApiResponse<Page<TravelListResponseDto>> getMyCreatedTravels(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // 여행 목록 조회
        Page<TravelListResponseDto> travelList = travelListService.getTravelListByUser(userNumber, pageable);

        return ApiResponse.success(travelList);
    }
}
