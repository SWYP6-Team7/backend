package swyp.swyp6_team7.travel.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.ClientIpAddressUtil;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.dto.TravelDetailLoginMemberRelatedDto;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelCreateResponse;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.dto.response.TravelUpdateResponse;
import swyp.swyp6_team7.travel.service.TravelService;
import swyp.swyp6_team7.travel.service.TravelViewCountService;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TravelController {

    private final TravelService travelService;
    private final TravelViewCountService travelViewCountService;

    // 여행 생성
    @PostMapping("/api/travel")
    public ApiResponse<TravelCreateResponse> create(
            @RequestBody @Valid TravelCreateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        Travel createdTravel = travelService.create(request, userNumber);
        TravelCreateResponse response = new TravelCreateResponse(createdTravel.getNumber());
        return ApiResponse.success(response);
    }

    // 여행 조회
    @GetMapping("/api/travel/detail/{travelNumber}")
    public ApiResponse<TravelDetailResponse> getDetailsByNumber(
            @PathVariable("travelNumber") int travelNumber,
            @RequireUserNumber Integer userNumber,
            HttpServletRequest request
    ) {
        TravelDetailResponse travelDetails = travelService.getDetailsByNumber(travelNumber); // 여행 상세 정보

        // 로그인 사용자 추가 정보
        if (userNumber != null) {
            TravelDetailLoginMemberRelatedDto loginMemberRelatedInfo = travelService
                    .getTravelDetailMemberRelatedInfo(userNumber, travelDetails.getTravelNumber(), travelDetails.getUserNumber(), travelDetails.getPostStatus());
            travelDetails.updateLoginMemberRelatedInfo(loginMemberRelatedInfo);
        }

        // 조회수 update
        // userIdentifier: userNumber(로그인) / IP address + User agent(비로그인)
        String userIdentifier;
        if (userNumber != null) {
            userIdentifier = userNumber.toString();
        } else {
            String ipAddress = ClientIpAddressUtil.getClientIp(request);
            String userBrowser = request.getHeader("User-Agent");
            userIdentifier = ipAddress + userBrowser;
        }
        travelViewCountService.updateViewCount(travelNumber, userIdentifier);

        return ApiResponse.success(travelDetails);
    }

    // 여행 수정
    @PutMapping("/api/travel/{travelNumber}")
    public ApiResponse<TravelUpdateResponse> update(
            @PathVariable("travelNumber") int travelNumber,
            @RequestBody @Valid TravelUpdateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        Travel updatedTravel = travelService.update(travelNumber, request, userNumber);
        TravelUpdateResponse response = new TravelUpdateResponse(updatedTravel.getNumber());
        return ApiResponse.success(response);
    }

    // 여행 삭제
    @DeleteMapping("/api/travel/{travelNumber}")
    public ApiResponse<String> delete(
            @PathVariable("travelNumber") int travelNumber,
            @RequireUserNumber Integer userNumber
    ) {
        travelService.delete(travelNumber, userNumber);
        return ApiResponse.success("여행이 삭제되었습니다.");
    }
}
