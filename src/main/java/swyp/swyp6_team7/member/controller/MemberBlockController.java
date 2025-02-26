package swyp.swyp6_team7.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.member.dto.ReportReasonResponse;
import swyp.swyp6_team7.member.dto.UserBlockDetailResponse;
import swyp.swyp6_team7.member.dto.UserReportRequest;
import swyp.swyp6_team7.member.service.MemberBlockService;

import java.util.List;

@Slf4j
@Tag(name = "유저 신고/차단 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/block")
public class MemberBlockController {

    private final MemberBlockService memberBlockService;

    @Operation(summary = "상대방 신고")
    @PostMapping("")
    public ApiResponse<String> report(
            @RequireUserNumber Integer userNumber,
            @RequestBody UserReportRequest request
    ) {
        memberBlockService.report(
                userNumber,
                request.getReportedUserNumber(),
                request.getReportReasonId(),
                request.getReportReasonExtra()
        );
        return ApiResponse.success("정상 처리되었습니다.");
    }

    @Operation(summary = "신고 사유 목록")
    @GetMapping("/reason/all")
    public ApiResponse<List<ReportReasonResponse>> getAllReportReasons() {
        return ApiResponse.success(memberBlockService.getAllReportReason());
    }

    @Operation(summary = "내 신고 상세")
    @GetMapping("/my/detail")
    public ApiResponse<UserBlockDetailResponse> myBlockDetail(
            @RequireUserNumber Integer userNumber
    ) {
        return ApiResponse.success(memberBlockService.getBlockDetail(userNumber));
    }

    @Operation(summary = "신고 사유 문의")
    @PostMapping("/request")
    public ApiResponse<Boolean> request() {
        return ApiResponse.success(true);
    }
}
