package swyp.swyp6_team7.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultProfileImage {

    // 디폴트 프로필 이미지 이름
    DEFAULT_PROFILE_1("defaultProfile.png"),
    DEFAULT_PROFILE_2("defaultProfile2.png"),
    DEFAULT_PROFILE_3("defaultProfile3.png"),
    DEFAULT_PROFILE_4("defaultProfile4.png"),
    DEFAULT_PROFILE_5("defaultProfile5.png"),
    DEFAULT_PROFILE_6("defaultProfile6.png");

    private final String imageName;

}
