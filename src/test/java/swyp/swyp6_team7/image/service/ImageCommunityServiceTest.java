package swyp.swyp6_team7.image.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import swyp.swyp6_team7.community.domain.Community;
import swyp.swyp6_team7.community.repository.CommunityRepository;
import swyp.swyp6_team7.image.domain.Image;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.repository.ImageRepository;
import swyp.swyp6_team7.image.s3.S3Uploader;
import swyp.swyp6_team7.image.util.S3KeyHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class ImageCommunityServiceTest {

    @Autowired
    private ImageCommunityService imageCommunityService;

    @Autowired
    private ImageRepository imageRepository;

    @MockBean
    private CommunityRepository communityRepository;

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

    @DisplayName("getCommunityImages: 커뮤니티 게시물에 포함되는 이미지들을 가져올 수 있다.")
    @Test
    void getCommunityImages() {
        // given
        int postNumber = 2;
        Image image1 = createImage("image1.png", postNumber, 1);
        Image image2 = createImage("image2.png", postNumber, 2);
        Image image3 = createImage("image3.png", postNumber, 3);
        imageRepository.saveAll(List.of(image1, image2, image3));

        // when
        List<ImageDetailResponseDto> imageDetails = imageCommunityService.getCommunityImages(postNumber);

        // then
        assertThat(imageDetails).hasSize(3)
                .extracting("relatedType", "relatedNumber", "key", "url", "uploadDate")
                .containsExactlyInAnyOrder(
                        tuple("community", 2, "baseFolder/community/2/1/stored-image1.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/1/stored-image1.png", LocalDateTime.of(2024, 11, 29, 12, 0)),
                        tuple("community", 2, "baseFolder/community/2/2/stored-image2.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/2/stored-image2.png", LocalDateTime.of(2024, 11, 29, 12, 0)),
                        tuple("community", 2, "baseFolder/community/2/3/stored-image3.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/3/stored-image3.png", LocalDateTime.of(2024, 11, 29, 12, 0))
                );
    }

    @DisplayName("updateCommunityImages: 커뮤니티 게시글의 이미지를 수정할 수 있다.")
    @Test
    void updateCommunityImages() {
        // given
        int postNumber = 2;
        int requestUserNumber = 1;
        Image image1 = imageRepository.save(createImage("image1.png", postNumber, 1));
        Image image2 = imageRepository.save(createImage("image2.png", postNumber, 2));
        Image image3 = imageRepository.save(createTempImage("image3.png"));

        List<String> status = List.of("d", "y", "i");
        List<String> urls = List.of(image1.getUrl(), image2.getUrl(), image3.getUrl());

        Community post = Community.builder()
                .userNumber(1)
                .build();
        given(communityRepository.findByPostNumber(anyInt())).willReturn(Optional.of(post));

        given(s3Uploader.existObject(anyString())).willReturn(true);
        given(s3Uploader.moveImage(anyString(), anyString()))
                .willReturn("");
        doNothing().when(s3Uploader).deleteFile(anyString());

        given(s3KeyHandler.generateS3Key("community", 2, "stored-image2.png", 1))
                .willReturn("baseFolder/community/2/1/stored-image2.png");
        given(s3KeyHandler.generateS3Key("community", 2, "stored-image3.png", 2))
                .willReturn("baseFolder/community/2/2/stored-image3.png");

        given(s3Uploader.getImageUrl("baseFolder/community/2/1/stored-image2.png"))
                .willReturn("https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/1/stored-image2.png");
        given(s3Uploader.getImageUrl("baseFolder/community/2/2/stored-image3.png"))
                .willReturn("https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/2/stored-image3.png");

        // when
        List<ImageDetailResponseDto> imageDetails = imageCommunityService.updateCommunityImages(postNumber, requestUserNumber, status, urls);

        // then
        assertThat(imageRepository.findAll()).hasSize(2)
                .extracting("imageNumber", "relatedType", "relatedNumber", "order", "key", "url", "originalName", "storageName", "size", "format")
                .containsExactlyInAnyOrder(
                        tuple(image2.getImageNumber(), "community", postNumber, 1, "baseFolder/community/2/1/stored-image2.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/1/stored-image2.png", "image2.png", "stored-image2.png", 2266L, "image/png"),
                        tuple(image3.getImageNumber(), "community", postNumber, 2, "baseFolder/community/2/2/stored-image3.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/2/stored-image3.png", "image3.png", "stored-image3.png", 2266L, "image/png")
                );

        assertThat(imageDetails).hasSize(2)
                .extracting("relatedType", "relatedNumber", "key", "url")
                .containsExactlyInAnyOrder(
                        tuple("community", 2, "baseFolder/community/2/1/stored-image2.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/1/stored-image2.png"),
                        tuple("community", 2, "baseFolder/community/2/2/stored-image3.png", "https://bucketName.s3.ap-northeast-2.amazonaws.com/baseFolder/community/2/2/stored-image3.png")
                );
    }

    @DisplayName("updateCommunityImages: d, n, y, i 이외의 status가 주어질 때 예외가 발생한다.")
    @Test
    void updateCommunityImagesWithInvalidStatus() {
        // given
        int postNumber = 2;
        int requestUserNumber = 1;
        Image image1 = imageRepository.save(createImage("image1.png", postNumber, 1));
        Image image2 = imageRepository.save(createImage("image2.png", postNumber, 2));
        Image image3 = imageRepository.save(createTempImage("image3.png"));

        List<String> status = List.of("k", "d", "i");
        List<String> urls = List.of(image1.getUrl(), image2.getUrl(), image3.getUrl());

        Community post = Community.builder()
                .userNumber(1)
                .build();
        given(communityRepository.findByPostNumber(anyInt())).willReturn(Optional.of(post));

        // when // then
        assertThatThrownBy(() -> {
            imageCommunityService.updateCommunityImages(postNumber, requestUserNumber, status, urls);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잘못된 status입니다. 가능한 status값: n(변경 없음), d(삭제), y(순서 변경), i(임시 저장)");
    }

    @DisplayName("updateCommunityImages: 게시글 작성자가 아닐 때 예외가 발생한다.")
    @Test
    void updateCommunityImagesWhenNotPostOwner() {
        // given
        int postNumber = 2;
        int requestUserNumber = 1;
        Image image1 = imageRepository.save(createImage("image1.png", postNumber, 1));
        Image image2 = imageRepository.save(createImage("image2.png", postNumber, 2));
        Image image3 = imageRepository.save(createTempImage("image3.png"));

        List<String> status = List.of("k", "y", "i");
        List<String> urls = List.of(image1.getUrl(), image2.getUrl(), image3.getUrl());

        Community post = Community.builder()
                .userNumber(3)
                .build();
        given(communityRepository.findByPostNumber(anyInt())).willReturn(Optional.of(post));

        // when // then
        assertThatThrownBy(() -> {
            imageCommunityService.updateCommunityImages(postNumber, requestUserNumber, status, urls);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커뮤니티 게시글 수정 권한이 없습니다.");
    }

    @DisplayName("deleteCommunityImages: 커뮤니티 게시글에 포함된 이미지 전체 삭제")
    @Test
    void deleteCommunityImages() {
        // given
        int postNumber = 2;
        int requestUserNumber = 1;
        Image image1 = createImage("image1.png", postNumber, 1);
        Image image2 = createImage("image2.png", postNumber, 2);
        Image image3 = createImage("image3.png", postNumber, 2);
        imageRepository.saveAll(List.of(image1, image2, image3));

        Community post = Community.builder()
                .userNumber(1)
                .build();
        given(communityRepository.findByPostNumber(anyInt())).willReturn(Optional.of(post));
        doNothing().when(s3Uploader).deleteFile(anyString());

        // when
        imageCommunityService.deleteCommunityImages(postNumber, requestUserNumber);

        // then
        assertThat(imageRepository.findAll()).isEmpty();
    }

    @DisplayName("deleteCommunityImages: 커뮤니티 게시글 작성자가 아닐 때 예외가 발생한다.")
    @Test
    void deleteCommunityImagesWhenNotPostOwner() {
        // given
        int postNumber = 2;
        int requestUserNumber = 1;
        Image image1 = createImage("image1.png", postNumber, 1);
        Image image2 = createImage("image2.png", postNumber, 2);
        Image image3 = createImage("image3.png", postNumber, 2);
        imageRepository.saveAll(List.of(image1, image2, image3));

        Community post = Community.builder()
                .userNumber(3)
                .build();
        given(communityRepository.findByPostNumber(anyInt())).willReturn(Optional.of(post));

        // when // then
        assertThatThrownBy(() -> {
            imageCommunityService.deleteCommunityImages(postNumber, requestUserNumber);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커뮤니티 이미지 삭제 권한이 없습니다.");
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

    private Image createImage(String imageName, int relatedNumber, int order) {
        String storageName = "stored-" + imageName;
        String key = "baseFolder/community/" + relatedNumber + "/" + order + "/" + storageName;
        return Image.builder()
                .originalName(imageName)
                .storageName(storageName)
                .size(2266L)
                .format("image/png")
                .relatedType("community")
                .relatedNumber(relatedNumber)
                .order(order)
                .key(key)
                .url("https://bucketName.s3.ap-northeast-2.amazonaws.com/" + key)
                .uploadDate(LocalDateTime.of(2024, 11, 29, 12, 0))
                .build();
    }

}