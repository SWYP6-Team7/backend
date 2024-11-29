package swyp.swyp6_team7.image.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;
import swyp.swyp6_team7.image.dto.request.TempDeleteRequestDto;
import swyp.swyp6_team7.image.dto.response.ImageDetailResponseDto;
import swyp.swyp6_team7.image.dto.response.ImageTempResponseDto;
import swyp.swyp6_team7.image.service.ImageProfileService;
import swyp.swyp6_team7.image.service.ImageService;
import swyp.swyp6_team7.mock.WithMockCustomUser;

import java.time.LocalDateTime;

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
class ImageProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImageProfileService imageProfileService;

    @MockBean
    private ImageService imageService;


    @DisplayName("createdProfileImage: 디폴트 이미지를 이용해 초기 프로필 이미지를 설정 및 생성할 수 있다.")
    @WithMockCustomUser(userNumber = 2)
    @Test
    void createdProfileImage() throws Exception {
        // given
        ImageDetailResponseDto response = ImageDetailResponseDto.builder()
                .imageNumber(1L)
                .relatedType("profile")
                .relatedNumber(2)
                .key("images/profile/default/defaultProfile.png")
                .url("https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png")
                .uploadDate(LocalDateTime.of(2024, 11, 24, 10, 0, 0))
                .build();

        given(imageProfileService.initializeDefaultProfileImage(anyInt()))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/profile/image"));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.imageNumber").value(1))
                .andExpect(jsonPath("$.relatedType").value("profile"))
                .andExpect(jsonPath("$.relatedNumber").value(2))
                .andExpect(jsonPath("$.key").value("images/profile/default/defaultProfile.png"))
                .andExpect(jsonPath("$.url").value("https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile.png"))
                .andExpect(jsonPath("$.uploadDate").value("2024년 11월 24일 10시 00분"));
        then(imageProfileService).should(times(1))
                .initializeDefaultProfileImage(eq(2));
    }

    @DisplayName("createTempImage: 프로필 이미지를 임시저장 할 수 있다.")
    @WithMockCustomUser
    @Test
    void createTempImage() throws Exception {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "temp_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "temp".getBytes()
        );

        ImageTempResponseDto response = new ImageTempResponseDto("https://temp_image.png");
        given(imageService.temporaryImage(any(MultipartFile.class)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/api/profile/image/temp")
                .file(mockFile));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.tempUrl").value("https://temp_image.png"));
        then(imageService).should(times(1)).temporaryImage(eq(mockFile));
    }

    @DisplayName("deleteTempImage: 임시 저장한 이미지를 삭제할 수 있다.")
    @WithMockCustomUser
    @Test
    void deleteTempImage() throws Exception {
        // given
        String tempUrl = "https://bucketName.s3.region.amazonaws.com/baseFolder/profile/temporary/storageName";
        TempDeleteRequestDto request = new TempDeleteRequestDto(tempUrl);

        doNothing().when(imageService).deleteTempImage(anyString());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/profile/image/temp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isNoContent());
        then(imageService).should(times(1)).deleteTempImage(eq(tempUrl));
    }

}