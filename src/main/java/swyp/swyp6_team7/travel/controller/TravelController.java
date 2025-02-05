package swyp.swyp6_team7.travel.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.ClientIpAddressUtil;
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

    private static final Logger logger = LoggerFactory.getLogger(TravelController.class);
    private final TravelService travelService;
    private final TravelViewCountService travelViewCountService;

    @PostMapping("/api/travel")
    public ResponseEntity<TravelCreateResponse> create(
            @RequestBody @Validated TravelCreateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        logger.info("Travel 생성 요청 - userId: {}", userNumber);

        Travel createdTravel = travelService.create(request, userNumber);
        logger.info("Travel 생성 완료 - userId: {}, createdTravel: {}", userNumber, createdTravel);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TravelCreateResponse(createdTravel.getNumber()));
    }

    @GetMapping("/api/travel/detail/{travelNumber}")
    public ResponseEntity<TravelDetailResponse> getDetailsByNumber(
            @PathVariable("travelNumber") int travelNumber,
            @RequireUserNumber Integer userNumber,
            HttpServletRequest request
    ) {

        // 여행 상세 정보
        TravelDetailResponse travelDetails = travelService.getDetailsByNumber(travelNumber);

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

        return ResponseEntity.status(HttpStatus.OK)
                .body(travelDetails);
    }

    @PutMapping("/api/travel/{travelNumber}")
    public ResponseEntity<TravelUpdateResponse> update(
            @PathVariable("travelNumber") int travelNumber,
            @RequestBody TravelUpdateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        logger.info("Travel 수정 요청 - userId: {}, travelNumber: {}", userNumber, travelNumber);

        Travel updatedTravel = travelService.update(travelNumber, request, userNumber);
        logger.info("Travel 수정 요청 - userId: {}, updatedTravel: {}", userNumber, updatedTravel);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new TravelUpdateResponse(updatedTravel.getNumber()));
    }

    @DeleteMapping("/api/travel/{travelNumber}")
    public ResponseEntity delete(
            @PathVariable("travelNumber") int travelNumber,
            @RequireUserNumber Integer userNumber
    ) {
        logger.info("Travel 삭제 요청 - userId: {}, travelNumber: {}", userNumber, travelNumber);

        travelService.delete(travelNumber, userNumber);
        logger.info("Travel 삭제 완료 - userId: {}, travelNumber: {}", userNumber, travelNumber);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
