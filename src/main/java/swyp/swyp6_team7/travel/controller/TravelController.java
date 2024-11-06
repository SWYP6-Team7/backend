package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.dto.request.TravelCreateRequest;
import swyp.swyp6_team7.travel.dto.request.TravelUpdateRequest;
import swyp.swyp6_team7.travel.dto.response.TravelDetailResponse;
import swyp.swyp6_team7.travel.service.TravelService;

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
                .body(travelService.getDetailsByNumber(createdTravel.getNumber(), loginUserNumber));
    }

    @GetMapping("/api/travel/detail/{travelNumber}")
    public ResponseEntity<TravelDetailResponse> getDetailsByNumber(
            @PathVariable("travelNumber") int travelNumber
    ) {
        Integer loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        logger.info("Travel 상세 조회 요청 - userId: {}, travelNumber: {}", loginUserNumber, travelNumber);

        TravelDetailResponse travelDetails = travelService.getDetailsByNumber(travelNumber, loginUserNumber);
        travelService.addViewCount(travelNumber); //조회수 update

        return ResponseEntity.status(HttpStatus.OK)
                .body(travelDetails);
    }

    @PutMapping("/api/travel/{travelNumber}")
    public ResponseEntity<TravelDetailResponse> update(
            @PathVariable("travelNumber") int travelNumber,
            @RequestBody TravelUpdateRequest request
    ) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        logger.info("Travel 수정 요청 - userId: {}, travelNumber: {}", loginUserNumber, travelNumber);

        Travel updatedTravel = travelService.update(travelNumber, request, loginUserNumber);
        logger.info("Travel 수정 완료 - updatedTravel: {}", updatedTravel);

        return ResponseEntity.status(HttpStatus.OK)
                .body(travelService.getDetailsByNumber(travelNumber, loginUserNumber));
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
