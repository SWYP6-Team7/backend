package swyp.swyp6_team7.profile.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.profile.dto.TargetUserProfileResponse;
import swyp.swyp6_team7.profile.service.TargetUserProfileService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class TargetUserProfileController {
    private final TargetUserProfileService targetUserProfileService;
    // 상대방 프로필 조회
    @GetMapping("/{targetUserNumber}/profile")
    public ApiResponse<TargetUserProfileResponse> viewOtherUserProfile(
            @RequireUserNumber Integer userNumber,
            @PathVariable("targetUserNumber") Integer targetUserNumber
    ){
        try {
            log.info("상대방 프로필 조회 요청 - 요청자 userNumber: {}, 대상 otherUserNumber: {}", userNumber, targetUserNumber);
            TargetUserProfileResponse response = targetUserProfileService.getOtherUserProfile(targetUserNumber);
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다.";
            log.warn("Invalid request: {}", errorMessage);
            throw new MoingApplicationException(errorMessage);
        } catch (Exception e) {
            log.error("Error fetching other user profile ", e);
            throw e;
        }
    }
}
