package swyp.swyp6_team7.profile.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.profile.dto.TargetUserProfileResponse;
import swyp.swyp6_team7.profile.dto.ProfileUpdateRequest;
import swyp.swyp6_team7.profile.dto.ProfileViewResponse;
import swyp.swyp6_team7.profile.service.ProfileService;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    // 프로필 수정 (이름, 자기소개, 선호 태그)
    @PutMapping("/update")
    public ApiResponse<String> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        try {
            log.info("프로필 수정 요청 확인");
            profileService.updateProfile(userNumber, request);

            log.info("프로필 수정 완료 - userNumber: {}", userNumber);
            return ApiResponse.success("프로필 업데이트 완료");
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다.";
            log.warn("Invalid request: {}", errorMessage);
            throw new MoingApplicationException(errorMessage);
        } catch (Exception e) {
            log.error("프로필 수정 중 에러발생", e);
            throw e;
        }
    }

    //내 프로필 조회
    @GetMapping("/me")
    public ApiResponse<ProfileViewResponse> viewProfile(
            @RequireUserNumber Integer userNumber
    ) {
        try {
            log.info("프로필 조회 요청");
            Optional<Users> userOpt = profileService.getUserByUserNumber(userNumber);

            if (userOpt.isEmpty()) {
                throw new MoingApplicationException("사용자를 찾을 수 없음");
            }

            // 실제 계산 로직이 없으므로, 0 또는 기본값으로 설정
            double travelDistance = 0.0;
            int visitedCountryCount = 0;
            int travelBadgeCount = 0;

            // DTO 생성자에 맞춰서 값 전달
            ProfileViewResponse response = new ProfileViewResponse(
                    userOpt.get(),
                    travelDistance,
                    visitedCountryCount,
                    travelBadgeCount
            );

            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다.";
            log.warn("Invalid request: {}", errorMessage);
            throw new MoingApplicationException(errorMessage);
        } catch (Exception e) {
            log.error("Error fetching profile ", e);
            throw e;
        }
    }

    // 상대방 프로필 조회
    @GetMapping("/{targetUserNumber}")
    public ApiResponse<TargetUserProfileResponse> viewOtherUserProfile(
            @RequireUserNumber Integer userNumber,
            @PathVariable("targetUserNumber") Integer targetUserNumber
    ){
        try {
            log.info("상대방 프로필 조회 요청 - 요청자 userNumber: {}, 대상 otherUserNumber: {}", userNumber, targetUserNumber);
            TargetUserProfileResponse response = profileService.getOtherUserProfile(targetUserNumber);
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
