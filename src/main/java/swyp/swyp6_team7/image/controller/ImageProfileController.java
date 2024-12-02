package swyp.swyp6_team7.image.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.image.dto.request.ImageUpdateByDefaultProfileRequest;
import swyp.swyp6_team7.image.dto.request.TempDeleteRequestDto;
import swyp.swyp6_team7.image.dto.request.TempUploadRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.dto.response.ImageTempResponseDto;
import swyp.swyp6_team7.image.service.ImageProfileService;
import swyp.swyp6_team7.image.service.ImageService;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/profile/image")
public class ImageProfileController {

    private final ImageService imageService;
    private final MemberService memberService;
    private final ImageProfileService imageProfileService;
    private final JwtProvider jwtProvider;

    //todo: MemberAuthorizeUtil.getLoginUserNumber로 전부 수정

    //초기 프로필 등록
    @PostMapping("")
    public ResponseEntity<ImageDetailResponseDto> createProfileImage() {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();

        ImageDetailResponseDto response = imageProfileService.initializeDefaultProfileImage(loginUserNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    // 임시 저장: 내 정보 수정 페이지에서 이미지 선택 시 호출
    @PostMapping("/temp")
    public ResponseEntity<ImageTempResponseDto> createTempImage(@RequestParam(value = "file") MultipartFile file) {
        ImageTempResponseDto response = imageService.temporaryImage(file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    // 임시 저장 삭제
    @DeleteMapping("/temp")
    public ResponseEntity<String> deleteTempImage(@RequestBody TempDeleteRequestDto request) {
        imageService.deleteTempImage(request.getDeletedTempUrl());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 이미지 정식 저장: 새로운 이미지 파일로 프로필 수정
    @PutMapping("")
    public ResponseEntity<ImageDetailResponseDto> updateProfileImage(@RequestBody TempUploadRequestDto request) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        ImageDetailResponseDto response = imageProfileService.uploadProfileImage(loginUserNumber, request.getImageUrl());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    //default 이미지 중 하나로 프로필 이미지 수정
    @PutMapping("/default")
    public ResponseEntity<ImageDetailResponseDto> updateProfileImageByDefaultImage(@RequestBody ImageUpdateByDefaultProfileRequest request) {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        ImageDetailResponseDto response = imageProfileService.updateByDefaultImage(loginUserNumber, request.getDefaultNumber());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    //프로필 이미지 데이터 삭제
    @DeleteMapping("")
    public ResponseEntity<Void> delete(Principal principal) {
        //user number 가져오기
        int userNumber = memberService.findByUserNumber(jwtProvider.getUserNumber(principal.getName())).getUserNumber();

        try {
            imageService.deleteImage("profile", userNumber);
            // 성공 시 204
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // 기타 오류 발생 시 500 Internal Server Error 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    //프로필 이미지 조회
    @GetMapping("")
    public ResponseEntity<ImageDetailResponseDto> getProfileImage() {
        int loginUserNumber = MemberAuthorizeUtil.getLoginUserNumber();
        ImageDetailResponseDto response = imageService.getImageDetail("profile", loginUserNumber, 0);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

}
