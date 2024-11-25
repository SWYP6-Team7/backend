package swyp.swyp6_team7.image.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
class ImageProfileServiceTest {

    @Autowired
    private ImageProfileService imageProfileService;

    @Autowired
    private ImageRepository imageRepository;


    @AfterEach
    void tearDown() {
        imageRepository.deleteAllInBatch();
    }

    @DisplayName("initializeDefaultProfileImage: 디폴트 이미지로 프로필 이미지를 생성하고 저장한다.")
    @Test
    void initializeDefaultProfileImage() {
        // given
        int userNumber = 1;

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


}