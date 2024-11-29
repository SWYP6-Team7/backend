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
import swyp.swyp6_team7.image.util.S3KeyHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@SpringBootTest
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @MockBean
    private S3Uploader s3Uploader;

    @MockBean
    private S3KeyHandler s3KeyHandler;

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
                .willReturn("https://bucketName.s3.region.amazonaws.com/baseFolder/profile/temporary/storageName");

        // when
        ImageTempResponseDto tempResponseDto = imageService.temporaryImage(mockFile);

        // then
        assertThat(tempResponseDto.getTempUrl()).isEqualTo("https://bucketName.s3.region.amazonaws.com/baseFolder/profile/temporary/storageName");
    }

    @DisplayName("deleteTempImage: 임시 저장 파일 URL이 주어질 때, S3 key를 찾고 파일을 삭제한다.")
    @Test
    void deleteTempImage() {
        // given
        String tempUrl = "https://bucketName.s3.region.amazonaws.com/baseFolder/profile/temporary/storageName";

        given(s3KeyHandler.getKeyByUrl(eq(tempUrl)))
                .willReturn("baseFolder/profile/temporary/storageName");

        // when
        imageService.deleteTempImage(tempUrl);

        // then
        then(s3KeyHandler).should(times(1)).getKeyByUrl(eq(tempUrl));
        then(s3Uploader).should(times(1)).deleteFile(eq("baseFolder/profile/temporary/storageName"));
    }

}