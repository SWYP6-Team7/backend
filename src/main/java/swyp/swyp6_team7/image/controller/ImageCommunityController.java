package swyp.swyp6_team7.image.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.image.dto.request.CommunityImageSaveRequest;
import swyp.swyp6_team7.image.dto.request.CommunityImageUpdateRequest;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.service.ImageCommunityService;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/community")
public class ImageCommunityController {

    private final ImageCommunityService imageCommunityService;

    // 임시 저장: 사용자가 커뮤니티 작성 페이지에 이미지를 추가하는 시점에 호출
    @PostMapping("/images/temp")
    public ApiResponse<ImageDetailResponseDto> uploadTempImage(@RequestParam(value = "file") MultipartFile file) {
        ImageDetailResponseDto response = imageCommunityService.uploadTempImage(file);
        return ApiResponse.success(response);
    }

    // 정식 저장: 커뮤니티 글이 게시되는 시점에 호출
    @PostMapping("/{postNumber}/images")
    public ApiResponse<List<ImageDetailResponseDto>> saveImages(
            @PathVariable(name = "postNumber") int postNumber,
            @RequestBody CommunityImageSaveRequest request
    ) {
        List<ImageDetailResponseDto> responses = imageCommunityService.saveCommunityImages(postNumber, request.getDeletedTempUrls(), request.getTempUrls());
        return ApiResponse.success(responses);
    }

    // 커뮤니티 게시글 이미지 조회
    @GetMapping("/{postNumber}/images")
    public ApiResponse<List<ImageDetailResponseDto>> getImages(@PathVariable(name = "postNumber") int postNumber) {
        List<ImageDetailResponseDto> responses = imageCommunityService.getCommunityImages(postNumber);
        return ApiResponse.success(responses);
    }

    // 커뮤니티 게시글 이미지 수정
    @PutMapping("/{postNumber}/images")
    public ApiResponse<List<ImageDetailResponseDto>> updateImages(
            @PathVariable(name = "postNumber") int postNumber,
            @RequestBody CommunityImageUpdateRequest request,
            @RequireUserNumber Integer userNumber
    ) {
        List<ImageDetailResponseDto> responses = imageCommunityService.updateCommunityImages(postNumber, userNumber, request.getStatuses(), request.getUrls());
        return ApiResponse.success(responses);
    }

    // 커뮤니티 게시글 이미지 삭제
    @DeleteMapping("/{postNumber}/images")
    public ApiResponse<Void> deleteImages(
            @PathVariable(name = "postNumber") int postNumber,
            @RequireUserNumber Integer userNumber
    ) {
        imageCommunityService.deleteCommunityImages(postNumber, userNumber);
        return ApiResponse.success(null);
    }
}