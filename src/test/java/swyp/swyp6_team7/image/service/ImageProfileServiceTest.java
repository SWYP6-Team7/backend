package swyp.swyp6_team7.image.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@SpringBootTest
class ImageProfileServiceTest {

    @Autowired
    private ImageProfileService imageProfileService;

    @Autowired
    private ImageRepository imageRepository;

    @MockBean
    private S3KeyHandler s3KeyHandler;

    @MockBean
    private S3Uploader s3Uploader;


    @AfterEach
    void tearDown() {
        imageRepository.deleteAllInBatch();
    }

    @DisplayName("initializeDefaultProfileImage: 디폴트 이미지로 프로필 이미지를 생성하고 저장한다.")
    @Test
    void initializeDefaultProfileImage() {
        // given
        int userNumber = 1;

        given(s3KeyHandler.getKeyByUrl(anyString()))
                .willReturn("images/profile/default/defaultProfile.png");

        // when
        ImageDetailResponseDto detailResponseDto = imageProfileService.initializeDefaultProfileImage(userNumber);

        // then
        assertThat(imageRepository.findAll()).hasSize(1)
                .extracting("originalName", "storageName", "size", "format", "relatedType", "relatedNumber", "order", "key", "url")
                .contains(
                        tuple(null, null, 0L, null, "profile", 1, 0, "images/profile/default/defaultProfile.png", "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png")
                );

        assertThat(detailResponseDto)
                .extracting("relatedType", "relatedNumber", "key", "url")
                .contains("profile", 1, "images/profile/default/defaultProfile.png", "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png");
    }

    @DisplayName("updateProfileByDefaultUrl: 번호가 주어질 때, 사용자 프로필 이미지를 디폴트 프로필 이미지로 변경한다.")
    @Test
    void updateProfileByDefaultUrl() {
        // given
        int userNumber = 2;
        Image userProfileImage = imageRepository.save(createImage(userNumber, "originalPicture"));

        given(s3KeyHandler.isFileUploadProfileImage(anyString(), anyInt()))
                .willReturn(true);
        doNothing().when(s3Uploader).deleteFile(anyString());
        given(s3KeyHandler.getKeyByUrl(anyString()))
                .willReturn("images/profile/default/defaultProfile4.png");

        // when
        ImageDetailResponseDto updatedProfileImage = imageProfileService.updateByDefaultImage(userNumber, 4);

        // then
        assertThat(imageRepository.findAll()).hasSize(1)
                .extracting("originalName", "storageName", "size", "format", "relatedType", "relatedNumber", "order", "key", "url")
                .contains(
                        tuple(null, null, null, null, "profile", 2, 0, "images/profile/default/defaultProfile4.png", "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile4.png")
                );

        assertThat(updatedProfileImage)
                .extracting("imageNumber", "relatedType", "relatedNumber", "key", "url")
                .contains(userProfileImage.getImageNumber(), "profile", 2, "images/profile/default/defaultProfile4.png", "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile4.png");
    }

    @DisplayName("uploadProfileImage: 임시 저장 이미지의 URL이 주어질 때, 해당 이미지로 프로필 이미지를 변경한다.")
    @Test
    void uploadProfileImage() {
        // given
        int userNumber = 2;
        Image userProfileImage = imageRepository.save(createImage(userNumber, "originalPicture"));

        String tempUrl = "https://bucketName.s3.region.amazonaws.com/baseFolder/profile/temporary/targetImage.png";

        given(s3KeyHandler.isFileUploadProfileImage(anyString(), anyInt()))
                .willReturn(true);
        doNothing().when(s3Uploader).deleteFile(anyString());
        given(s3KeyHandler.getKeyByUrl(anyString()))
                .willReturn("baseFolder/profile/temporary/targetImage.png");
        given(s3KeyHandler.generateS3Key(anyString(), anyInt(), anyString(), anyInt()))
                .willReturn("baseFolder/profile/2/targetImage.png");
        given(s3Uploader.moveImage(anyString(), anyString()))
                .willReturn("baseFolder/profile/2/targetImage.png");
        given(s3Uploader.getImageUrl(anyString()))
                .willReturn("https://bucketName.s3.region.amazonaws.com/baseFolder/profile/2/targetImage.png");

        // when
        ImageDetailResponseDto updatedProfileImage = imageProfileService.uploadProfileImage(userNumber, tempUrl);

        // then
        assertThat(imageRepository.findAll()).hasSize(1)
                .extracting("originalName", "storageName", "size", "format", "relatedType", "relatedNumber", "order", "key", "url")
                .contains(
                        tuple(null, null, null, null, "profile", 2, 0, "baseFolder/profile/2/targetImage.png", "https://bucketName.s3.region.amazonaws.com/baseFolder/profile/2/targetImage.png")
                );

        assertThat(updatedProfileImage)
                .extracting("imageNumber", "relatedType", "relatedNumber", "key", "url")
                .contains(userProfileImage.getImageNumber(), "profile", 2, "baseFolder/profile/2/targetImage.png", "https://bucketName.s3.region.amazonaws.com/baseFolder/profile/2/targetImage.png");

        then(s3Uploader).should(times(1)).deleteFile(anyString());
    }

    @DisplayName("deleteProfileImage: 사용자 이미지를 디폴트 이미지로 변경한다.")
    @Test
    void deleteProfileImage() {
        // given
        int userNumber = 2;
        Image image = imageRepository.save(createImage(userNumber, "fileUploadImage"));

        given(s3KeyHandler.isFileUploadProfileImage(anyString(), anyInt()))
                .willReturn(true);
        doNothing().when(s3Uploader).deleteFile(anyString());
        given(s3KeyHandler.getKeyByUrl(anyString()))
                .willReturn("images/profile/default/defaultProfile.png");

        // when
        imageProfileService.deleteProfileImage(userNumber);

        // then
        assertThat(imageRepository.findAll()).hasSize(1)
                .extracting("imageNumber", "originalName", "storageName", "size", "format", "relatedType", "relatedNumber", "order", "key", "url")
                .contains(
                        tuple(image.getImageNumber(), null, null, null, null, "profile", 2, 0, "images/profile/default/defaultProfile.png", "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png")
                );
    }

    private Image createImage(int relatedNumber, String originalName) {
        String storageName = originalName + "storageName.png";
        String key = "baseFolder/profile/" + relatedNumber + "/" + storageName;
        return Image.builder()
                .originalName(originalName + "png")
                .storageName(storageName)
                .size(2266L)
                .format("image/png")
                .relatedType("profile")
                .relatedNumber(relatedNumber)
                .order(0)
                .key(key)
                .url("https://bucketName.s3.region.amazonaws.com/" + key)
                .uploadDate(LocalDateTime.of(2024, 11, 29, 12, 0))
                .build();
    }

}