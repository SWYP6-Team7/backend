package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.travel.dto.response.TravelRecentDto;
import swyp.swyp6_team7.travel.dto.response.TravelRecommendResponse;
import swyp.swyp6_team7.travel.service.TravelHomeService;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
public class TravelHomeController {

    private final TravelHomeService travelHomeService;

    @GetMapping("/api/travels/recent")
    public ApiResponse<Page<TravelRecentDto>> getRecentlyCreatedTravels(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber
    ) {

        Page<TravelRecentDto> result = travelHomeService
                .getTravelsSortedByCreatedAt(PageRequest.of(page, size), userNumber);

        return ApiResponse.success(result);
    }

    @GetMapping("/api/travels/recommend")
    public ApiResponse<Page<TravelRecommendResponse>> getRecommendTravels(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequireUserNumber Integer userNumber
    ) {
        LocalDate requestDate = LocalDate.now();

        // 로그인 사용자: 태그 기반 추천 + 마감일이 빠른 순서 + title
        if (userNumber != null) {
            Page<TravelRecommendResponse> result = travelHomeService
                    .getRecommendTravelsByMember(PageRequest.of(page, size), userNumber, requestDate)
                    .map(TravelRecommendResponse::new);

            return ApiResponse.success(result);
        }

        // 비로그인 사용자: 북마크 개수 기반 추천 + title
        Page<TravelRecommendResponse> result = travelHomeService
                .getRecommendTravelsByNonMember(PageRequest.of(page, size), requestDate)
                .map(TravelRecommendResponse::new);

        return ApiResponse.success(result);
    }
}
