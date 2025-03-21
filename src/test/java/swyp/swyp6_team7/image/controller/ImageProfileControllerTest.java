package swyp.swyp6_team7.image.controller;

import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.auth.dto.LoginTokenResponse;
import swyp.swyp6_team7.config.RedisContainerConfig;
import swyp.swyp6_team7.global.IntegrationTest;
import swyp.swyp6_team7.image.dto.request.ImageUpdateByDefaultProfileRequest;
import swyp.swyp6_team7.image.dto.request.TempDeleteRequestDto;
import swyp.swyp6_team7.image.dto.request.TempUploadRequestDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Import(RedisContainerConfig.class)
class ImageProfileControllerTest extends IntegrationTest {

    private String accessToken;

    private void setup() {
        createUser("test", "password");
        LoginTokenResponse tokenResponse = login("test@test.com", "password");
        accessToken = tokenResponse.getAccessToken();
    }

    @DisplayName("createProfileImage: 디폴트 이미지를 이용해 초기 프로필 이미지를 설정 및 생성할 수 있다.")
    @Order(1)
    @Test
    void createProfileImage() throws Exception {
        // when
        setup();
        ResultActions resultActions = mockMvc.perform(post("/api/profile/image")
                .header("Authorization", "Bearer " + accessToken));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.imageNumber").value(1))
                .andExpect(jsonPath("$.success.relatedType").value("profile"))
                .andExpect(jsonPath("$.success.relatedNumber").value(1))
                .andExpect(jsonPath("$.success.key").value("images/profile/default/defaultProfile.png"))
                .andExpect(jsonPath("$.success.url").value("https://bucket-name.s3.region.amazonaws.com/images/profile/default/defaultProfile.png"));
    }

    @DisplayName("createTempImage: 프로필 이미지를 임시저장 할 수 있다.")
    @Transactional
    @Order(2)
    @Test
    void createTempImage() throws Exception {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "temp_image.png",
                MediaType.IMAGE_PNG_VALUE,
                "temp".getBytes()
        );

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/api/profile/image/temp")
                .file(mockFile)
                .header("Authorization", "Bearer " + accessToken));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.tempUrl").value("https://temp_image.png"));
    }

    @DisplayName("deleteTempImage: 임시 저장한 이미지를 삭제할 수 있다.")
    @Order(5)
    @Test
    void deleteTempImage() throws Exception {
        // given
        String tempUrl = "https://bucket-name.s3.region.amazonaws.com/baseFolder/profile/temporary/storageName";
        TempDeleteRequestDto request = new TempDeleteRequestDto(tempUrl);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/profile/image/temp")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @DisplayName("updateProfileImage: 주어지는 URL로 프로필 이미지를 수정한다.")
    @Test
    @Order(4)
    void updateProfileImage() throws Exception {
        // given
        String imageUrl = "https://bucket-name.s3.region.amazonaws.com/baseFolder/profile/temporary/storageName";
        TempUploadRequestDto request = new TempUploadRequestDto(imageUrl);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/profile/image")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.imageNumber").value(1))
                .andExpect(jsonPath("$.success.relatedType").value("profile"))
                .andExpect(jsonPath("$.success.relatedNumber").value(2))
                .andExpect(jsonPath("$.success.key").value("baseFolder/profile/temporary/storageName"))
                .andExpect(jsonPath("$.success.url").value("https://bucketName.s3.region.amazonaws.com/baseFolder/profile/temporary/storageName"))
                .andExpect(jsonPath("$.success.uploadDate").value("2024년 11월 24일 10시 00분"));
    }

    @DisplayName("updateProfileImageByDefaultImage: 디폴트 프로필 이미지 중 하나로 프로필 이미지를 변경한다.")
    @Order(6)
    @Test
    void updateProfileImageByDefaultImage() throws Exception {
        // given
        ImageUpdateByDefaultProfileRequest request = new ImageUpdateByDefaultProfileRequest(4);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/profile/image/default")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.imageNumber").value(1))
                .andExpect(jsonPath("$.success.relatedType").value("profile"))
                .andExpect(jsonPath("$.success.relatedNumber").value(2))
                .andExpect(jsonPath("$.success.key").value("images/profile/default/defaultProfile4.png"))
                .andExpect(jsonPath("$.success.url").value("https://moing-hosted-contents.s3.ap-northeast-2.amazonaws.com/images/profile/default/defaultProfile4.png"))
                .andExpect(jsonPath("$.success.uploadDate").value("2024년 11월 24일 10시 00분"));
    }

    @DisplayName("delete: 사용자의 프로필 이미지 데이터를 삭제한다.")
    @Order(7)
    @Test
    void deleteProfile() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/profile/image")
                .header("Authorization", "Bearer " + accessToken));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @DisplayName("getProfileImage: 사용자의 프로필 이미지를 조회한다.")
    @Order(3)
    @Test
    void getProfileImage() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/profile/image")
                .header("Authorization", "Bearer " + accessToken));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success.imageNumber").value(1))
                .andExpect(jsonPath("$.success.relatedType").value("profile"))
                .andExpect(jsonPath("$.success.relatedNumber").value(2))
                .andExpect(jsonPath("$.success.key").value("baseFolder/profile/1/storageName.png"))
                .andExpect(jsonPath("$.success.url").value("https://bucketName.s3.region.amazonaws.com/baseFolder/profile/1/storageName.png"))
                .andExpect(jsonPath("$.success.uploadDate").value("2024년 11월 24일 10시 00분"));
    }

}