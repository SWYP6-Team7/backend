package swyp.swyp6_team7.image.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import swyp.swyp6_team7.image.dto.response.ImageTempResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @MockBean
    private S3Uploader s3Uploader;

    @AfterEach
    void tearDown() {
        imageRepository.deleteAllInBatch();
    }


    @DisplayName("temporaryImage: 주어지는 이미지를 S3에 업로드하고 임시 저장 경로 URL을 반환한다.")
    @Test
    void temporaryImage() {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "temp_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "temp".getBytes()
        );

        given(s3Uploader.uploadInTemporary(any(), anyString()))
                .willReturn("baseFolder/profile/temporary/storageName");
        given(s3Uploader.getImageUrl(anyString()))
                .willReturn("http://bucketName.s3.region/baseFolder/profile/temporary/storageName");

        // when
        ImageTempResponseDto tempResponseDto = imageService.temporaryImage(mockFile);

        // then
        assertThat(tempResponseDto.getTempUrl()).isEqualTo("http://bucketName.s3.region/baseFolder/profile/temporary/storageName");
    }

}