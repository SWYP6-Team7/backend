package swyp.swyp6_team7.image.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityImageSaveRequest {

    // 임시저장 이미지 중 정식 등록 하지 않는 이미지 URL
    private List<String> deletedTempUrls;

    // 임시저장 이미지 중 정식 등록 할 이미지 URL
    private List<String> tempUrls;

}
