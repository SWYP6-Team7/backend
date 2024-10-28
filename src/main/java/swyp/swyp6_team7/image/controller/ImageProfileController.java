package swyp.swyp6_team7.image.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.image.dto.request.ImageDefaultRequestDto;
import swyp.swyp6_team7.image.dto.request.TempDeleteRequestDto;
import swyp.swyp6_team7.image.dto.request.TempUploadRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.dto.response.ImageTempResponseDto;
import swyp.swyp6_team7.image.service.ImageProfileService;
import swyp.swyp6_team7.image.service.ImageService;
import swyp.swyp6_team7.member.service.MemberService;

import java.io.IOException;
import java.security.Principal;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/profile/image")
public class ImageProfileController {

    private final ImageService imageService;
    private final MemberService memberService;
    private final ImageProfileService imageProfileService;


    //초기 프로필 등록
    @PostMapping("")
    public ResponseEntity<ImageDetailResponseDto> createdProfileImage(Principal principal) {

        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        ImageDetailResponseDto response = imageProfileService.initializeDefaultProfileImage(userNumber);
        return ResponseEntity.ok(response);
    }

    //임시 저장
    @PostMapping("/temp")
    public ResponseEntity<ImageTempResponseDto> createTempImage (@RequestParam(value = "file") MultipartFile file, Principal principal) throws IOException{

        ImageTempResponseDto response = imageService.temporaryImage(file);
        return ResponseEntity.ok(response);
    }

    //임시 저장 삭제
    @DeleteMapping("/temp")
    public ResponseEntity<String> deleteTempImage(@RequestBody TempDeleteRequestDto request, Principal principal) {

        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();


        try {
            imageService.deleteTempImage(request.getDeletedTempUrl());
            // 성공 시 204
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // 기타 오류 발생 시 500 Internal Server Error 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //새로운 이미지 파일로 프로필 수정 (정식 저장)
    @PutMapping("")
    public ResponseEntity<ImageDetailResponseDto> updatedProfileImage(@RequestBody TempUploadRequestDto request, Principal principal){
        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        ImageDetailResponseDto response = imageProfileService.uploadProfileImage("profile", userNumber, request.getImageUrl());

        return ResponseEntity.ok(response);
    }

    //default 이미지 중 하나로 프로필 이미지 수정
    @PutMapping("/default")
    public ResponseEntity<ImageDetailResponseDto> updateDefaultImage(@RequestBody ImageDefaultRequestDto request, Principal principal) {

        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        ImageDetailResponseDto response = imageProfileService.updateProfileByDefaultUrl(userNumber, request.getDefaultNumber());
        return ResponseEntity.ok(response);
    }

    //프로필 이미지 데이터 삭제
    @DeleteMapping("")
    public ResponseEntity<Void> delete(Principal principal) {
        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

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
    public ResponseEntity<ImageDetailResponseDto> getProfileImage(Principal principal) {
        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        ImageDetailResponseDto response = imageService.getImageDetailByNumber("Profile", userNumber, 0);

        return ResponseEntity.ok(response);
    }

}
