package swyp.swyp6_team7.image.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.image.dto.request.ImageCommunityRequestDto;
import swyp.swyp6_team7.image.dto.request.ImageSaveRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.service.ImageCommunityService;
import swyp.swyp6_team7.member.service.MemberService;

import java.io.IOException;
import java.security.Principal;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/community")
public class ImageCommunityController {
    private final ImageCommunityService imageCommunityService;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    //이미지 임시 저장
    @PostMapping("/images/temp")

    public ResponseEntity<ImageDetailResponseDto> uploadTemporaryImage(
            @RequestParam(value = "file") MultipartFile file) throws IOException {

        ImageDetailResponseDto response = imageCommunityService.uploadTempImage(file);
        return ResponseEntity.ok(response);
    }

    //이미지 정식 저장
    @PostMapping("/{postNumber}/images")
    public ResponseEntity<ImageDetailResponseDto[]> saveImages(
            @PathVariable int postNumber,
            @RequestBody ImageSaveRequestDto allUrls
    ) {

        ImageDetailResponseDto[] responses = imageCommunityService.saveCommunityImage(postNumber, allUrls.getDeletedTempUrls(), allUrls.getTempUrls());
        return ResponseEntity.ok(responses);
    }

    // 게시글 별 이미지 조회
    @GetMapping("/{postNumber}/images")
    public ResponseEntity<ImageDetailResponseDto[]> getImages(@PathVariable int postNumber) {

        ImageDetailResponseDto[] responses = imageCommunityService.communityImageDetail(postNumber);

        return ResponseEntity.ok(responses);
    }

    //게시글 별 이미지 수정
    @PutMapping("/{postNumber}/images")
    public ResponseEntity<ImageDetailResponseDto[]> updateImages(
            @PathVariable int postNumber, @RequestBody ImageCommunityRequestDto request, Principal principal) {

        //user number 가져오기
        int userNumber = memberService.findByUserNumber(jwtProvider.getUserNumber(principal.getName())).getUserNumber();

        ImageDetailResponseDto[] responses = imageCommunityService.updateCommunityImage(postNumber, request.getStatuses(), request.getUrls(), userNumber);
        return ResponseEntity.ok(responses);
    }

    //게시글 별 이미지 삭제
    @DeleteMapping("/{postNumber}/images")
    public ResponseEntity<Void> deleteImages(@PathVariable int postNumber, Principal principal) {

        //user number 가져오기
        int userNumber = memberService.findByUserNumber(jwtProvider.getUserNumber(principal.getName())).getUserNumber();

        // 게시글 작성자와 같은 사람인지 확인 필요

        try {
            imageCommunityService.deleteCommunityImage("community", postNumber, userNumber);
            // 성공 시 204
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // 기타 오류 발생 시 500 Internal Server Error 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}