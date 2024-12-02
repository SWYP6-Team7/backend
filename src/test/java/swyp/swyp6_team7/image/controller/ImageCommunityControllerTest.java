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
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.service.ImageCommunityService;
import swyp.swyp6_team7.image.service.ImageProfileService;
import swyp.swyp6_team7.image.service.ImageService;
import swyp.swyp6_team7.mock.WithMockCustomUser;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

}