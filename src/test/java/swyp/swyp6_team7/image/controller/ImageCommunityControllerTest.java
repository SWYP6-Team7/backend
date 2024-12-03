package swyp.swyp6_team7.image.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import swyp.swyp6_team7.image.dto.request.CommunityImageSaveRequest;
import swyp.swyp6_team7.image.dto.request.CommunityImageUpdateRequest;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.service.ImageCommunityService;
import swyp.swyp6_team7.mock.WithMockCustomUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImageCommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImageCommunityService imageCommunityService;


    @DisplayName("uploadTempImage: 커뮤니티 이미지를 임시저장한다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    void uploadTempImage() throws Exception {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "community_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "temp".getBytes()
        );

        ImageDetailResponseDto response = ImageDetailResponseDto.builder()
                .imageNumber(1L)
                .relatedType("community")
                .relatedNumber(0)
                .key("baseFolder/community/temporary/storageName.png")
                .url("https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName.png")
                .uploadDate(LocalDateTime.of(2024, 11, 24, 10, 0, 0))
                .build();
        given(imageCommunityService.uploadTempImage(any()))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/api/community/images/temp")
                .file(mockFile));

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageNumber").value(1))
                .andExpect(jsonPath("$.relatedType").value("community"))
                .andExpect(jsonPath("$.relatedNumber").value(0))
                .andExpect(jsonPath("$.key").value("baseFolder/community/temporary/storageName.png"))
                .andExpect(jsonPath("$.url").value("https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName.png"))
                .andExpect(jsonPath("$.uploadDate").value("2024년 11월 24일 10시 00분"));
    }

    @DisplayName("saveImages: 임시 저장 커뮤니티 이미지를 정식으로 저장한다.")
    @WithMockCustomUser
    @Test
    void saveImages() throws Exception {
        // given
        int postNumber = 2;
        List<String> deletedUrls = List.of("https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName.png");
        List<String> tempUrls = List.of(
                "https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName2.png",
                "https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName3.png");
        CommunityImageSaveRequest request = new CommunityImageSaveRequest(deletedUrls, tempUrls);

        ImageDetailResponseDto image1 = createdDetailResponse(1, postNumber,
                "baseFolder/community/temporary/storageName2.png",
                "https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName2.png");
        ImageDetailResponseDto image2 = createdDetailResponse(2, postNumber,
                "baseFolder/community/temporary/storageName3.png",
                "https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName3.png");
        List<ImageDetailResponseDto> response = List.of(image1, image2);

        given(imageCommunityService.saveCommunityImages(anyInt(), anyList(), anyList()))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/community/{postNumber}/images", postNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].imageNumber").value(1L))
                .andExpect(jsonPath("$.[0].relatedType").value("community"))
                .andExpect(jsonPath("$.[0].relatedNumber").value(2))
                .andExpect(jsonPath("$.[0].key").value("baseFolder/community/temporary/storageName2.png"))
                .andExpect(jsonPath("$.[0].url").value("https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName2.png"))
                .andExpect(jsonPath("$.[0].uploadDate").value("2024년 11월 24일 10시 00분"))
                .andExpect(jsonPath("$.[1].imageNumber").value(2L))
                .andExpect(jsonPath("$.[1].relatedType").value("community"))
                .andExpect(jsonPath("$.[1].relatedNumber").value(2))
                .andExpect(jsonPath("$.[1].key").value("baseFolder/community/temporary/storageName3.png"))
                .andExpect(jsonPath("$.[1].url").value("https://bucketName.s3.region.amazonaws.com/baseFolder/community/temporary/storageName3.png"))
                .andExpect(jsonPath("$.[1].uploadDate").value("2024년 11월 24일 10시 00분"));
        then(imageCommunityService).should(times(1))
                .saveCommunityImages(eq(postNumber), eq(deletedUrls), eq(tempUrls));
    }

    @DisplayName("getImages: 커뮤니티 게시글에 포함되는 이미지들을 조회한다.")
    @WithMockCustomUser
    @Test
    void getImages() throws Exception {
        // given
        int postNumber = 2;
        ImageDetailResponseDto image = createdDetailResponse(1, postNumber,
                "baseFolder/community/2/1/storageName2.png",
                "https://bucketName.s3.region.amazonaws.com/baseFolder/community/2/1/storageName2.png");
        List<ImageDetailResponseDto> response = List.of(image);

        given(imageCommunityService.getCommunityImages(anyInt()))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/community/{postNumber}/images", postNumber));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].imageNumber").value(1L))
                .andExpect(jsonPath("$.[0].relatedType").value("community"))
                .andExpect(jsonPath("$.[0].relatedNumber").value(postNumber))
                .andExpect(jsonPath("$.[0].key").value("baseFolder/community/2/1/storageName2.png"))
                .andExpect(jsonPath("$.[0].url").value("https://bucketName.s3.region.amazonaws.com/baseFolder/community/2/1/storageName2.png"))
                .andExpect(jsonPath("$.[0].uploadDate").value("2024년 11월 24일 10시 00분"));
    }

    @DisplayName("updateImages: 커뮤니티 게시글에 포함되는 이미지를 수정한다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    void updateImages() throws Exception {
        // given
        int postNumber = 2;
        List<String> status = List.of("d", "y");
        List<String> urls = List.of(
                "https://bucketName.s3.region.amazonaws.com/baseFolder/community/2/1/storageName1.png",
                "https://bucketName.s3.region.amazonaws.com/baseFolder/community/2/2/storageName2.png"
        );
        CommunityImageUpdateRequest request = new CommunityImageUpdateRequest(status, urls);

        ImageDetailResponseDto image = createdDetailResponse(1, postNumber,
                "baseFolder/community/2/1/storageName2.png",
                "https://bucketName.s3.region.amazonaws.com/baseFolder/community/2/1/storageName2.png");
        List<ImageDetailResponseDto> response = List.of(image);

        given(imageCommunityService.updateCommunityImages(postNumber, 2, status, urls))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/community/{postNumber}/images", postNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].imageNumber").value(1L))
                .andExpect(jsonPath("$.[0].relatedType").value("community"))
                .andExpect(jsonPath("$.[0].relatedNumber").value(postNumber))
                .andExpect(jsonPath("$.[0].key").value("baseFolder/community/2/1/storageName2.png"))
                .andExpect(jsonPath("$.[0].url").value("https://bucketName.s3.region.amazonaws.com/baseFolder/community/2/1/storageName2.png"))
                .andExpect(jsonPath("$.[0].uploadDate").value("2024년 11월 24일 10시 00분"));
    }

    @DisplayName("deleteImages: 커뮤니티 게시글의 이미지를 삭제한다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    void deleteImages() throws Exception {
        // given
        int postNumber = 2;
        doNothing().when(imageCommunityService).deleteCommunityImages(anyInt(), anyInt());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/community/{postNumber}/images", postNumber));

        // then
        resultActions.andExpect(status().isNoContent());
        then(imageCommunityService).should().deleteCommunityImages(postNumber, 2);
    }

    private ImageDetailResponseDto createdDetailResponse(long imageNumber, int relatedNumber, String key, String url) {
        return ImageDetailResponseDto.builder()
                .imageNumber(imageNumber)
                .relatedType("community")
                .relatedNumber(relatedNumber)
                .key(key)
                .url("https://bucketName.s3.region.amazonaws.com/" + key)
                .uploadDate(LocalDateTime.of(2024, 11, 24, 10, 0, 0))
                .build();
    }
}