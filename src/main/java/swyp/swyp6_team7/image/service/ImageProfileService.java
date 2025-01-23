package swyp.swyp6_team7.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.image.domain.DefaultProfileImage;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.ImageCreateDto;
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

    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;
    private final StorageNameHandler storageNameHandler;
    private final S3KeyHandler s3KeyHandler;


    // 프로필 생성 시 이미지 처리
    @Transactional
    public ImageDetailResponseDto initializeDefaultProfileImage(int userNumber) {
        if (imageRepository.existsProfileImageByUserNumber(userNumber)) {
            throw new IllegalArgumentException("이미 프로필 이미지가 존재합니다.");
        }

        String defaultImageUrl = s3KeyHandler.getDefaultProfileImageUrl(DefaultProfileImage.DEFAULT_PROFILE_1.getImageName());
        String defaultImageKey = s3KeyHandler.getKeyByUrl(defaultImageUrl);

        ImageCreateDto imageCreateDto = ImageCreateDto.builder()
                .relatedType("profile")
                .relatedNumber(userNumber)
                .order(0)
                .key(defaultImageKey)
                .url(defaultImageUrl)
                .uploadDate(LocalDateTime.now())
                .build();

        Image uploadedImage = imageRepository.save(imageCreateDto.toImageEntity());
        return ImageDetailResponseDto.from(uploadedImage);
    }


    // 프로필 이미지 변경: 기본 이미지 중 하나로 변경
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
        DefaultProfileImage defaultProfileImage = null;
        switch (defaultProfileImageNumber) {
            case 1:
                defaultProfileImage = DefaultProfileImage.DEFAULT_PROFILE_1;
                break;
            case 2:
                defaultProfileImage = DefaultProfileImage.DEFAULT_PROFILE_2;
                break;
            case 3:
                defaultProfileImage = DefaultProfileImage.DEFAULT_PROFILE_3;
                break;
            case 4:
                defaultProfileImage = DefaultProfileImage.DEFAULT_PROFILE_4;
                break;
            case 5:
                defaultProfileImage = DefaultProfileImage.DEFAULT_PROFILE_5;
                break;
            case 6:
                defaultProfileImage = DefaultProfileImage.DEFAULT_PROFILE_6;
                break;
            default:
                log.warn("유효하지 않은 Default Profile Image Number입니다: {}", defaultProfileImageNumber);
                throw new IllegalArgumentException("유효하지 않은 Default Profile Image Number입니다: " + defaultProfileImageNumber);
        }
        return s3KeyHandler.getDefaultProfileImageUrl(defaultProfileImage.getImageName());
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

    @Transactional
    public void deleteProfileImage(int relatedNumber) {
        Image image = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder("profile", relatedNumber, 0)
                .orElseThrow(() -> {
                    log.warn("Profile Image Not Found. relatedNumber: {}", relatedNumber);
                    return new IllegalArgumentException("Profile Image Not Found.");
                });
        String presentKey = image.getKey();

        // 이전 이미지가 파일 업로드인 경우에만 S3 이미지 삭제 후 디폴트 이미지로 변경
        if (s3KeyHandler.isFileUploadProfileImage(presentKey, relatedNumber)) {
            s3Uploader.deleteFile(presentKey); // s3 이미지 삭제

            String defaultImageUrl = s3KeyHandler.getDefaultProfileImageUrl(DefaultProfileImage.DEFAULT_PROFILE_1.getImageName());
            String key = s3KeyHandler.getKeyByUrl(defaultImageUrl);
            image.updateWithUrl(key, defaultImageUrl, LocalDateTime.now());
        }
    }

}
