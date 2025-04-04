package swyp.swyp6_team7.image.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.image.dto.request.ImageUpdateByDefaultProfileRequest;
import swyp.swyp6_team7.image.dto.request.TempDeleteRequestDto;
import swyp.swyp6_team7.image.dto.request.TempUploadRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.dto.response.ImageTempResponseDto;
import swyp.swyp6_team7.image.service.ImageProfileService;
import swyp.swyp6_team7.image.service.ImageService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/profile/image")
public class ImageProfileController {

    private final ImageService imageService;
    private final ImageProfileService imageProfileService;

    //초기 프로필 등록
    @PostMapping("")
    public ApiResponse<ImageDetailResponseDto> createProfileImage(
            @RequireUserNumber Integer userNumber
    ) {
        ImageDetailResponseDto response = imageProfileService.initializeDefaultProfileImage(userNumber);
        return ApiResponse.success(response);
    }

    // 임시 저장: 내 정보 수정 페이지에서 이미지 선택 시 호출
    @PostMapping("/temp")
    public ApiResponse<ImageTempResponseDto> createTempImage(@RequestParam(value = "file") MultipartFile file) {
        ImageTempResponseDto response = imageService.temporaryImage(file);
        return ApiResponse.success(response);
    }

    // 임시 저장 삭제
    @DeleteMapping("/temp")
    public ApiResponse<Void> deleteTempImage(@RequestBody TempDeleteRequestDto request) {
        imageService.deleteTempImage(request.getDeletedTempUrl());
        return ApiResponse.success(null);
    }

    // 이미지 정식 저장: 새로운 이미지 파일로 프로필 수정
    @PutMapping("")
    public ApiResponse<ImageDetailResponseDto> updateProfileImage(
            @RequestBody TempUploadRequestDto request,
            @RequireUserNumber Integer userNumber
    ) {
        ImageDetailResponseDto response = imageProfileService.uploadProfileImage(userNumber, request.getImageUrl());
        return ApiResponse.success(response);
    }

    //default 이미지 중 하나로 프로필 이미지 수정
    @PutMapping("/default")
    public ApiResponse<ImageDetailResponseDto> updateProfileImageByDefaultImage(
            @RequestBody ImageUpdateByDefaultProfileRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        ImageDetailResponseDto response = imageProfileService.updateByDefaultImage(userNumber, request.getDefaultNumber());
        return ApiResponse.success(response);
    }

    //프로필 이미지 s3 데이터 삭제 후 디폴트 이미지로 설정
    @DeleteMapping("")
    public ApiResponse<Void> delete(
            @RequireUserNumber Integer userNumber
    ) {
        imageProfileService.deleteProfileImage(userNumber);
        return ApiResponse.success(null);
    }

    //프로필 이미지 조회
    @GetMapping("")
    public ApiResponse<ImageDetailResponseDto> getProfileImage(
            @RequireUserNumber Integer loginUserNumber
    ) {
        log.info("loginUserNumber: {}", loginUserNumber);
        ImageDetailResponseDto response = imageService.getImageDetail("profile", loginUserNumber, 0);
        return ApiResponse.success(response);
    }
}
