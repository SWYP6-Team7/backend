package swyp.swyp6_team7.image.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class ImageCommunityServiceTest {

    @Autowired
    private ImageCommunityService imageCommunityService;

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

    @DisplayName("uploadTempImage: 커뮤니티 이미지를 임시 저장 할 수 있다.")
    @Test
    void uploadTempImage() {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "community_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "temp".getBytes()
        );

        given(s3Uploader.uploadInTemporary(any(), anyString()))
                .willReturn("baseFolder/community/temporary/storageName.png");
        given(s3Uploader.getImageUrl(anyString()))
                .willReturn("https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName.png");

        // when
        ImageDetailResponseDto response = imageCommunityService.uploadTempImage(mockFile);

        // then
        assertThat(imageRepository.findAll()).hasSize(1)
                .extracting("originalName", "storageName", "size", "format", "relatedType", "relatedNumber", "order", "key", "url")
                .contains(
                        tuple("community_image.png", "storageName.png", 4L, "image/png", "community", 0, 0, "baseFolder/community/temporary/storageName.png", "https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName.png")
                );
        assertThat(response)
                .extracting("relatedType", "relatedNumber", "key", "url")
                .contains("community", 0, "baseFolder/community/temporary/storageName.png", "https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName.png");
    }

    @DisplayName("saveCommunityImages: 커뮤니티 글에 대한 이미지 정식 저장")
    @Test
    void saveCommunityImages() {
        // given
        int postNumber = 2;
        Image image1 = imageRepository.save(createTempImage("image1"));
        Image image2 = imageRepository.save(createTempImage("image2"));
        Image image3 = imageRepository.save(createTempImage("image3"));

        List<String> deletedUrls = List.of(image1.getUrl());
        List<String> tempUrls = List.of(image2.getUrl(), image3.getUrl());

        given(s3Uploader.existObject(anyString())).willReturn(true);
        doNothing().when(s3Uploader).deleteFile(anyString());
        given(s3Uploader.moveImage(anyString(), anyString()))
                .willReturn("");

        given(s3KeyHandler.getKeyByUrl("https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/temporary/stored-image2"))
                .willReturn("baseFolder/community/temporary/stored-image2");
        given(s3KeyHandler.getKeyByUrl("https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/temporary/stored-image3"))
                .willReturn("baseFolder/community/temporary/stored-image3");
        given(s3KeyHandler.generateS3Key("community", 2, "stored-image2", 1))
                .willReturn("baseFolder/community/2/1/stored-image2.png");
        given(s3KeyHandler.generateS3Key("community", 2, "stored-image3", 2))
                .willReturn("baseFolder/community/2/2/stored-image3.png");

        given(s3Uploader.getImageUrl("baseFolder/community/2/1/stored-image2.png"))
                .willReturn("https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/1/stored-image2.png");
        given(s3Uploader.getImageUrl("baseFolder/community/2/2/stored-image3.png"))
                .willReturn("https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/2/stored-image3.png");

        // when
        List<ImageDetailResponseDto> savedImages = imageCommunityService.saveCommunityImages(postNumber, deletedUrls, tempUrls);

        // then
        assertThat(imageRepository.findAll()).hasSize(2)
                .extracting("imageNumber", "relatedType", "relatedNumber", "order", "key", "url", "originalName", "storageName", "size", "format")
                .containsExactlyInAnyOrder(
                        tuple(image2.getImageNumber(), "community", postNumber, 1, "baseFolder/community/2/1/stored-image2.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/1/stored-image2.png", "image2", "stored-image2", 2266L, "image/png"),
                        tuple(image3.getImageNumber(), "community", postNumber, 2, "baseFolder/community/2/2/stored-image3.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/2/stored-image3.png", "image3", "stored-image3", 2266L, "image/png")
                );

        assertThat(savedImages)
                .extracting("relatedType", "relatedNumber", "key", "url")
                .containsExactlyInAnyOrder(
                        tuple("community", 2, "baseFolder/community/2/1/stored-image2.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/1/stored-image2.png"),
                        tuple("community", 2, "baseFolder/community/2/2/stored-image3.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/2/stored-image3.png")
                );
    }

    private Image createTempImage(String imageName) {
        String storageName = "stored-" + imageName;
        String key = "baseFolder/community/temporary/" + storageName;
        return Image.builder()
                .originalName(imageName)
                .storageName(storageName)
                .size(2266L)
                .format("image/png")
                .relatedType("community")
                .relatedNumber(0)
                .order(0)
                .key(key)
                .url("https://bucketName.s3.ap-northeast-2.amazonaws.com/" + key)
                .uploadDate(LocalDateTime.of(2024, 11, 29, 12, 0))
                .build();
    }
}