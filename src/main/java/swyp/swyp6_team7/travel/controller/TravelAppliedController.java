package swyp.swyp6_team7.travel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.global.utils.auth.MemberAuthorizeUtil;
import swyp.swyp6_team7.travel.dto.response.TravelListResponseDto;
import swyp.swyp6_team7.travel.service.TravelAppliedService;

@RestController
@RequestMapping("/api/my-applied-travels")
@RequiredArgsConstructor
public class TravelAppliedController {

    private final TravelAppliedService travelAppliedService;
    private final JwtProvider jwtProvider;

    // 사용자가 신청한 여행 목록 조회
    @GetMapping("")
    public ResponseEntity<Page<TravelListResponseDto>> getAppliedTrips(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size

    ) {
        Pageable pageable = PageRequest.of(page, size);

        Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();

        // 여행 목록 조회
        Page<TravelListResponseDto> appliedTrips = travelAppliedService.getAppliedTripsByUser(userNumber, pageable);

        return ResponseEntity.ok(appliedTrips);
    }

    // 사용자가 특정 여행에 대한 참가 취소
    @DeleteMapping("/{travelNumber}/cancel")
    public ResponseEntity<Void> cancelTripApplication(@RequestHeader("Authorization") String token, @PathVariable("travelNumber") int travelNumber) {

        Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();

        // 참가 취소 처리
        travelAppliedService.cancelApplication(userNumber, travelNumber);
        return ResponseEntity.noContent().build();
    }
}
