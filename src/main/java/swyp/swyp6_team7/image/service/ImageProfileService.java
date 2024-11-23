package swyp.swyp6_team7.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.request.ImageCreateRequestDto;
import swyp.swyp6_team7.image.dto.request.ImageUpdateRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;
import swyp.swyp6_team7.image.util.StorageNameHandler;

import java.util.Optional;

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
    private final ImageService imageService;
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
    public ImageDetailResponseDto updateProfileByDefaultUrl(int relatedNumber, int defaultProfileImageNumber) {
        String relatedType = "profile";
        int order = 0;
        String url = "";

        Optional<Image> searchImage = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder(relatedType, relatedNumber, 0);

        // TODO: 메서드 분리
        switch (defaultProfileImageNumber) {
            case 1:
                url = DEFAULT_PROFILE_URL;
                //url = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png";
                break;
            case 2:
                url = DEFAULT_PROFILE_URL2;
                //url = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile2.png";
                break;
            case 3:
                url = DEFAULT_PROFILE_URL3;
                //url = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile3.png";
                break;
            case 4:
                url = DEFAULT_PROFILE_URL4;
                //url = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile4.png";
                break;
            case 5:
                url = DEFAULT_PROFILE_URL5;
                //url = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile5.png";
                break;
            case 6:
                url = DEFAULT_PROFILE_URL6;
                //url = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile6.png";
                break;
            default:
                log.warn("ImageProfileService - 유효하지 않은 default profile image number: {}", defaultProfileImageNumber);
                throw new IllegalArgumentException("유효하지 않은 default image number 입니다: " + defaultProfileImageNumber);
        }

        String presentKey = searchImage.get().getKey();

        //이전 이미지가 파일 업로드인지 default 이미지인지 확인
        //key가 "images/profile/relatedNumber"로 시작하면, 이전 이미지는 파일 업로드 이미지
        if (presentKey.startsWith("images/profile/" + relatedNumber)) {
            //이미지 삭제
            s3Uploader.deleteFile(presentKey);
        }
        //key가 "images/profile/default"로 시작하면, 이전 이미지는 디폴트 이미지
        else if (presentKey.startsWith("images/profile/default")) {
            //이미지 삭제 동작 필요 없음
        } else {
            throw new IllegalArgumentException("업데이트 전 DB  데이터에 오류가 있습니다.");
        }
        String defaultKey = s3KeyHandler.getKeyByUrl(url);
        // DB 업데이트 동작
        ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
                .relatedType(relatedType)
                .relatedNumber(relatedNumber)
                .order(0)
                .key(defaultKey)
                .url(url)
                .build();

        return imageService.updateDB(relatedType, relatedNumber, 0, updateRequest);
    }

    //기존 이미지를 지우고 url로 이미지 수정 (이미지 정식 저장)
    @Transactional
    public ImageDetailResponseDto uploadProfileImage(String relatedType, int relatedNumber, String tempUrl) {
        //DB에서 이미지 찾기
        Optional<Image> searchImage = imageRepository.findByRelatedTypeAndRelatedNumberAndOrder(relatedType, relatedNumber, 0);
        String presentKey = searchImage.get().getKey();

        //이전 이미지가 파일 업로드인지 default 이미지인지 확인
        //key가 "images/profile/relatedNumber"로 시작하면, 이전 이미지는 파일 업로드 이미지
        if (presentKey.startsWith("images/profile/" + relatedNumber)) {
            //이미지 삭제
            s3Uploader.deleteFile(presentKey);
        }
        //key가 "images/profile/default"로 시작하면, 이전 이미지는 디폴트 이미지
        else if (presentKey.startsWith("images/profile/default")) {
            //이미지 삭제 동작 필요 없음
        } else {
            throw new IllegalArgumentException("업데이트 전 DB 데이터에 오류가 있습니다.");
        }

        //storage name 추출
        String storageName = storageNameHandler.extractStorageName(s3KeyHandler.getKeyByUrl(tempUrl));
        //sourceKey 추출

        String tempKey = s3KeyHandler.getKeyByUrl(tempUrl);
        //새로운 key 생성
        String newKey = s3KeyHandler.generateS3Key(relatedType, relatedNumber, storageName, 0);

        //정식 경로로 이동
        s3Uploader.moveImage(tempKey, newKey);
        //새로운 url 추출
        String newUrl = s3Uploader.getImageUrl(newKey);

        // DB 업데이트 동작
        ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
                .relatedType(relatedType)
                .relatedNumber(relatedNumber)
                .order(0)
                .key(newKey)
                .url(newUrl)
                .build();

        return imageService.updateDB(relatedType, relatedNumber, 0, updateRequest);

    }
}
