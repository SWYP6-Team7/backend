package swyp.swyp6_team7.image.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import swyp.swyp6_team7.config.DataConfig;
import swyp.swyp6_team7.image.domain.Image;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import(DataConfig.class)
@DataJpaTest
class ImageCustomRepositoryImplTest {

    @Autowired
    private ImageRepository imageRepository;

    @DisplayName("existsProfileImageByUserNumber: 해당 유저의 프로필 이미지가 존재하면 true를 반환한다.")
    @Test
    void existsProfileImage() {
        // given
        Integer userNumber = 10;
        Image image = createImage(userNumber, "profileImage");
        imageRepository.save(image);

        // when
        boolean result = imageRepository.existsProfileImageByUserNumber(userNumber);

        // then
        assertThat(result).isEqualTo(true);
    }

    @DisplayName("existsProfileImageByUserNumber: 해당 유저의 프로필 이미지가 존재하면 false를 반환한다.")
    @Test
    void existsProfileImageWhenNoImage() {
        // given
        Integer userNumber = 10;

        // when
        boolean result = imageRepository.existsProfileImageByUserNumber(userNumber);

        // then
        assertThat(result).isEqualTo(false);
    }

    private Image createImage(int relatedNumber, String originalName) {
        String storageName = originalName + "StorageName.png";
        String key = "baseFolder/profile/" + relatedNumber + "/" + storageName;
        return Image.builder()
                .originalName(originalName + ".png")
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