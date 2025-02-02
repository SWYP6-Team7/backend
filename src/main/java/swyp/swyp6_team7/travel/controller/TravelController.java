package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.auth.MemberAuthorizeUtil;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.dto.TravelDetailLoginMemberRelatedDto;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelCreateResponse;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.dto.response.TravelUpdateResponse;
import swyp.swyp6_team7.travel.service.TravelService;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TravelController {

    private static final Logger logger = LoggerFactory.getLogger(TravelController.class);
    private final TravelService travelService;

    @PostMapping("/api/travel")
    public ResponseEntity<TravelCreateResponse> create(@RequestBody @Validated TravelCreateRequest request) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        logger.info("Travel 생성 요청 - userId: {}", loginUserNumber);

        Travel createdTravel = travelService.create(request, loginUserNumber);
        logger.info("Travel 생성 완료 - userId: {}, createdTravel: {}", loginUserNumber, createdTravel);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TravelCreateResponse(createdTravel.getNumber()));
    }

    @GetMapping("/api/travel/detail/{travelNumber}")
    public ResponseEntity<TravelDetailResponse> getDetailsByNumber(@PathVariable("travelNumber") int travelNumber) {

        // 여행 상세 정보
        TravelDetailResponse travelDetails = travelService.getDetailsByNumber(travelNumber);
        travelService.addViewCount(travelNumber); //조회수 update

        // 로그인 사용자 추가 정보
        Integer loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        if (loginUserNumber != null) {
            TravelDetailLoginMemberRelatedDto loginMemberRelatedInfo = travelService
                    .getTravelDetailMemberRelatedInfo(loginUserNumber, travelDetails.getTravelNumber(), travelDetails.getUserNumber(), travelDetails.getPostStatus());
            travelDetails.updateLoginMemberRelatedInfo(loginMemberRelatedInfo);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(travelDetails);
    }

    @PutMapping("/api/travel/{travelNumber}")
    public ResponseEntity<TravelUpdateResponse> update(
            @PathVariable("travelNumber") int travelNumber,
            @RequestBody TravelUpdateRequest request
    ) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        logger.info("Travel 수정 요청 - userId: {}, travelNumber: {}", loginUserNumber, travelNumber);

        Travel updatedTravel = travelService.update(travelNumber, request, loginUserNumber);
        logger.info("Travel 수정 요청 - userId: {}, updatedTravel: {}", loginUserNumber, updatedTravel);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new TravelUpdateResponse(updatedTravel.getNumber()));
    }

    @DeleteMapping("/api/travel/{travelNumber}")
    public ResponseEntity delete(@PathVariable("travelNumber") int travelNumber) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        logger.info("Travel 삭제 요청 - userId: {}, travelNumber: {}", loginUserNumber, travelNumber);

        travelService.delete(travelNumber, loginUserNumber);
        logger.info("Travel 삭제 완료 - userId: {}, travelNumber: {}", loginUserNumber, travelNumber);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
