package swyp.swyp6_team7.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.request.ImageCreateRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;
import swyp.swyp6_team7.image.util.StorageNameHandler;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ImageProfileService {

    // 디폴트 프로필 이미지 URL
    private final static String DEFAULT_PROFILE_URL = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png";
    private final static String DEFAULT_PROFILE_URL2 = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile2.png";
    private final static String DEFAULT_PROFILE_URL3 = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile3.png";
    private final static String DEFAULT_PROFILE_URL4 = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile4.png";
    private final static String DEFAULT_PROFILE_URL5 = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile5.png";
    private final static String DEFAULT_PROFILE_URL6 = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile6.png";

    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;
    private final StorageNameHandler storageNameHandler;
    private final S3KeyHandler s3KeyHandler;


    // 프로필 생성 시 이미지 처리
    @Transactional
    public ImageDetailResponseDto initializeDefaultProfileImage(int userNumber) {
        String defaultKey = s3KeyHandler.getKeyByUrl(DEFAULT_PROFILE_URL);

        // TODO: Image 클래스에서 create메서드 호출
        // TODO: save 후 결과물을 dto로 변환 후 반환
        //DB insert 동작
        ImageCreateRequestDto imageCreateDto = ImageCreateRequestDto.builder()
                .relatedType("profile")
                .relatedNumber(userNumber)
                .order(0)
                .key(defaultKey)
                .url(DEFAULT_PROFILE_URL)
                .build();

        // DB에 저장
        Image image = imageCreateDto.toImageEntity();
        Image uploadedImage = imageRepository.save(image);

        return new ImageDetailResponseDto(uploadedImage);
    }


    // 프로필 이미지 변경: 기본 이미지로 변경
    @Transactional
    public ImageDetailResponseDto updateByDefaultImage(int relatedNumber, int defaultProfileImageNumber) {
        String relatedType = "profile";
        Image searchImage = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder(relatedType, relatedNumber, 0)
                .orElseThrow(() -> {
                    log.warn("Profile Image Not Found. relatedNumber: {}", relatedNumber);
                    return new IllegalArgumentException("Profile Image Not Found.");
                });
        String presentKey = searchImage.getKey();

        // 이전 프로필 이미지가 default 이미지가 아니라 파일 업로드인 경우 삭제
        if (s3KeyHandler.isFileUploadProfileImage(presentKey, relatedNumber)) {
            s3Uploader.deleteFile(presentKey);
        }

        String defaultProfileImageUrl = getDefaultImageUrl(defaultProfileImageNumber);
        String defaultProfileImageKey = s3KeyHandler.getKeyByUrl(defaultProfileImageUrl);

        Image updatedProfileImage = searchImage.updateWithUrl(defaultProfileImageKey, defaultProfileImageUrl, LocalDateTime.now());
        return ImageDetailResponseDto.from(updatedProfileImage);
    }

    private String getDefaultImageUrl(int defaultProfileImageNumber) {
        String defaultProfileImageUrl = "";
        switch (defaultProfileImageNumber) {
            case 1:
                defaultProfileImageUrl = DEFAULT_PROFILE_URL;
                break;
            case 2:
                defaultProfileImageUrl = DEFAULT_PROFILE_URL2;
                break;
            case 3:
                defaultProfileImageUrl = DEFAULT_PROFILE_URL3;
                break;
            case 4:
                defaultProfileImageUrl = DEFAULT_PROFILE_URL4;
                break;
            case 5:
                defaultProfileImageUrl = DEFAULT_PROFILE_URL5;
                break;
            case 6:
                defaultProfileImageUrl = DEFAULT_PROFILE_URL6;
                break;
            default:
                log.warn("유효하지 않은 Default Profile Image Number입니다: {}", defaultProfileImageNumber);
                throw new IllegalArgumentException("유효하지 않은 Default Profile Image Number입니다: " + defaultProfileImageNumber);
        }
        return defaultProfileImageUrl;
    }

    // 프로필 이미지 변경(이미지 정식 저장): 기존 이미지 삭제 후, 주어지는 임시 저장 URL로 프로필 이미지 변경
    @Transactional
    public ImageDetailResponseDto uploadProfileImage(int relatedNumber, String tempUrl) {
        String relatedType = "profile";
        Image searchImage = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder(relatedType, relatedNumber, 0)
                .orElseThrow(() -> {
                    log.warn("Profile Image Not Found. relatedNumber: {}, url: {}", relatedNumber, tempUrl);
                    return new IllegalArgumentException("Profile Image Not Found.");
                });
        String presentKey = searchImage.getKey();

        // 이전 프로필 이미지가 default 이미지가 아니라 파일 업로드인 경우 삭제
        if (s3KeyHandler.isFileUploadProfileImage(presentKey, relatedNumber)) {
            s3Uploader.deleteFile(presentKey);
        }

        String tempKey = s3KeyHandler.getKeyByUrl(tempUrl); // S3 Key 추출: {baseFolder}/temporary/{storageName}
        String storageName = storageNameHandler.extractStorageName(tempKey); // storage name 추출
        String newKey = s3KeyHandler.generateS3Key(relatedType, relatedNumber, storageName, 0); // 새로운 Key 생성

        // 정식 경로로 이동
        s3Uploader.moveImage(tempKey, newKey);

        String newUrl = s3Uploader.getImageUrl(newKey); // 새로운 url 생성
        Image updatedProfileImage = searchImage.updateWithUrl(newKey, newUrl, LocalDateTime.now());

        return ImageDetailResponseDto.from(updatedProfileImage);
    }

}
