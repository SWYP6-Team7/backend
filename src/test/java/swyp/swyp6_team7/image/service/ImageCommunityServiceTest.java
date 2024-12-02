package swyp.swyp6_team7.image.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;
import swyp.swyp6_team7.image.util.StorageNameHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

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

}