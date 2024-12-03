package swyp.swyp6_team7.image.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityImageUpdateRequest {

    // n: 불변, d: 삭제, y: 순서 변경, i: 정식 저장
    private List<String> statuses;
    private List<String> urls;

}
