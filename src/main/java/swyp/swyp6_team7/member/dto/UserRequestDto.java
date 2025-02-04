package swyp.swyp6_team7.member.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class UserRequestDto {
    private String email;       // user_email
    private String password;    // user_pw
    private String name;   // user_name
    private String gender;      // user_gender (M/F)
    private String agegroup;   // user_age_group
    private Set<String> preferredTags; // 사용자 선호 태그 리스트

    private String sessionToken; // 회원가입 요청 Session Token

    // 관리자로 가입할 떄 사용하는 키
    @Value("${custom.admin-secret-key}")
    private String adminSecretKey;

    // 기본 생성자
    public UserRequestDto() {
    }

    public UserRequestDto(String email, String password, String name, String gender, String agegroup, Set<String> preferredTags) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.agegroup = agegroup;
        this.preferredTags = preferredTags;
    }

}
