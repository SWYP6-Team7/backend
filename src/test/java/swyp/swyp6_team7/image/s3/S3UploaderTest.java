package swyp.swyp6_team7.image.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import swyp.swyp6_team7.image.util.S3KeyHandler;
import swyp.swyp6_team7.image.util.StorageNameHandler;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@SpringBootTest
class S3UploaderTest {

    @Autowired
    private S3Uploader s3Uploader;

    @MockBean
    private AmazonS3 amazonS3;

    @MockBean
    private StorageNameHandler storageNameHandler;

    @MockBean
    private S3KeyHandler s3KeyHandler;


    @DisplayName("uploadInTemporary: 임시 저장 경로에 이미지 파일을 업로드하고 S3Key를 반환한다.")
    @Test
    void uploadInTemporary() throws Exception {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "temp_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "temp".getBytes()
        );

        given(storageNameHandler.generateUniqueFileName(anyString())).willReturn("storageName.png");
        given(s3KeyHandler.generateTempS3Key(anyString(), anyString()))
                .willReturn("baseFolder/profile/temporary/storageName");
        doReturn(null).when(amazonS3).putObject(any(PutObjectRequest.class));

        // when
        String s3Key = s3Uploader.uploadInTemporary(mockFile, "profile");

        // then
        assertThat(s3Key).isEqualTo("baseFolder/profile/temporary/storageName");
    }

    @DisplayName("uploadInTemporary: S3에 임시 저장 이미지 파일 업로드 실패 시 예외가 발생한다.")
    @Test
    void uploadInTemporaryWhenUploadFailed() {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "temp_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "temp".getBytes()
        );

        given(storageNameHandler.generateUniqueFileName(anyString())).willReturn("storageName.png");
        given(s3KeyHandler.generateTempS3Key(anyString(), anyString()))
                .willReturn("baseFolder/profile/temporary/storageName");
        given(amazonS3.putObject(any(PutObjectRequest.class)))
                .willThrow(new SdkClientException("Unable to verify integrity of data upload(...)"));

        // when // then
        assertThatThrownBy(() -> {
            s3Uploader.uploadInTemporary(mockFile, "profile");
        }).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("S3 파일 업로드 실패");
    }

    @DisplayName("deleteFile: S3 key가 주어질 때, AWS bucket에서 해당 파일을 삭제한다.")
    @Test
    void deleteFile() {
        // given
        String s3Key = "baseFolder/profile/temporary/storageName";

        given(amazonS3.doesObjectExist(anyString(), anyString()))
                .willReturn(true);
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());

        // when
        s3Uploader.deleteFile(s3Key);

        // then
        then(amazonS3).should(times(1)).deleteObject(anyString(), eq(s3Key));
    }

    @DisplayName("deleteFile: S3 key가 주어질 때, 파일이 존재하지 않으면 그냥 넘어간다.")
    @Test
    void deleteFileWhenObjectNotExist() {
        // given
        String s3Key = "baseFolder/profile/temporary/storageName";

        given(amazonS3.doesObjectExist(anyString(), anyString()))
                .willReturn(false);
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());

        // when
        s3Uploader.deleteFile(s3Key);

        // then
        then(amazonS3).should(times(0)).deleteObject(anyString(), eq(s3Key));
    }


    @DisplayName("getImageUrl: S3 Key로 URL을 가져온다.")
    @Test
    void getImageUrl() throws Exception {
        // given
        String s3Key = "baseFolder/profile/temporary/storageName";

        given(amazonS3.doesObjectExist(anyString(), anyString()))
                .willReturn(true);
        given(amazonS3.getUrl(anyString(), anyString()))
                .willReturn(new URL("http://bucketName.s3.region/baseFolder/profile/temporary/storageName"));

        // when
        String imageUrl = s3Uploader.getImageUrl(s3Key);

        // then
        assertThat(imageUrl).isEqualTo("http://bucketName.s3.region/baseFolder/profile/temporary/storageName");
        then(amazonS3).should(times(1)).getUrl(anyString(), eq(s3Key));
    }

    @DisplayName("getImageUrl: S3 경로에 이미지가 존재하지 않을 경우 빈 문자열을 반환한다.")
    @Test
    void getImageUrlWhenImageNotExist() {
        // given
        String s3Key = "baseFolder/profile/temporary/notExistName";

        given(amazonS3.doesObjectExist(anyString(), anyString()))
                .willReturn(false);

        // when
        String imageUrl = s3Uploader.getImageUrl(s3Key);

        // then
        assertThat(imageUrl).isEqualTo("");
        then(amazonS3).should(times(0)).getUrl(anyString(), eq(s3Key));
    }

    @DisplayName("copyImage: 동일한 bucket내에서 소스 경로의 이미지를 대상 경로에 복사할 수 있다.")
    @Test
    void copyImage() {
        // given
        String sourceKey = "/baseFolder/temporary/storageName.png";
        String destinationKey = "/baseFolder/profile/relatedNumber/storageName.png";

        given(amazonS3.copyObject(any(CopyObjectRequest.class)))
                .willReturn(any(CopyObjectResult.class));

        // when
        s3Uploader.copyImage(sourceKey, destinationKey);

        // then
        then(amazonS3).should(times(1))
                .copyObject(any(CopyObjectRequest.class));
    }

}
