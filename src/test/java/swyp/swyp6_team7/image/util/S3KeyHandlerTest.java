package swyp.swyp6_team7.image.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import swyp.swyp6_team7.image.s3.S3Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

class S3KeyHandlerTest {

    private S3KeyHandler s3KeyHandler;
    private S3Component s3ComponentMock;

    @BeforeEach
    void setup() {
        s3ComponentMock = Mockito.mock(S3Component.class);
        given(s3ComponentMock.getBaseFolder()).willReturn("mock-base-folder/");
        given(s3ComponentMock.getBucket()).willReturn("bucket-name");

        s3KeyHandler = new S3KeyHandler(s3ComponentMock);
    }

    @DisplayName("generateTempS3Key: 임시저장 S3 Key를 생성할 수 있다.")
    @Test
    void generateTempS3Key() {
        // given
        String relatedType = "profile";
        String storageName = "storage-name";

        // when
        String tempS3Key = s3KeyHandler.generateTempS3Key(relatedType, storageName);

        // then
        assertThat(tempS3Key).isEqualTo("mock-base-folder/profile/temporary/storage-name");
    }

    @DisplayName("generateTempS3Key: relatedType이 유효하지 않은 경우 예외가 발생한다.")
    @Test
    void generateTempS3KeyWithInvalidRelatedType() {
        // given
        String relatedType = "aa";
        String storageName = "storage-name";

        // when // then
        assertThatThrownBy(() -> {
            s3KeyHandler.generateTempS3Key(relatedType, storageName);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않는 타입입니다: " + relatedType);
    }

    @DisplayName("getKeyByUrl: 주어지는 URL이 설정된 s3UrlPrefix로 시작하지 않을 경우 예외가 발생한다.")
    @Test
    void getKeyByUrlWithInvalidS3UrlPrefix() {
        // given
        String url = "https://wrong-bucket-url/images/profile/default/defaultProfile.png";

        // when // then
        assertThatThrownBy(() -> {
            s3KeyHandler.getKeyByUrl(url);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("URL 형식이 올바르지 않습니다. S3 URL인지 확인해주세요.");
    }

    @DisplayName("isFileUploadProfileImage: key가 '{baseFolder}/profile/{relatedNumber}'로 시작하면 true를 반환한다.")
    @Test
    void isFileUploadProfileImage() {
        // given
        String s3Key = "mock-base-folder/profile/2/storageName.png";

        // when
        boolean result = s3KeyHandler.isFileUploadProfileImage(s3Key, 2);

        // then
        assertThat(result).isTrue();
    }
}