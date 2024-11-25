package swyp.swyp6_team7.image.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class S3KeyHandlerTest {

    @Autowired
    private S3KeyHandler s3KeyHandler;
    
    @DisplayName("getKeyByUrl: 주어지는 URL을 이용해 S3 Key를 추출할 수 있다.")
    @Test
    void getKeyByUrl() {
        // given
        String url = "https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png";
    
        // when
        String keyByUrl = s3KeyHandler.getKeyByUrl(url);

        // then
        assertThat(keyByUrl).isEqualTo("images/profile/default/defaultProfile.png");
    }

}