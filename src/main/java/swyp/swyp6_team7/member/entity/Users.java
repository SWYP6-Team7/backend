package swyp.swyp6_team7.member.entity;
import lombok.*;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.member.dto.UserRequestDto;
import swyp.swyp6_team7.tag.domain.Tag;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userNumber;

    @Column(nullable = false, unique = true, length = 255)
    private String userEmail;

    @Column(nullable = false, length = 255)
    private String userPw;

    @Column(nullable = false, length = 50)
    private String userName;

    // 성별 ENUM으로 관리
    public enum Gender{
        M,F
    }

    @Column(nullable = false, length = 2)
    @Enumerated(EnumType.STRING)
    private Gender userGender;

    public enum AgeGroup{
        TEEN("10대"), // 10대
        TWENTY("20대"), // 20대
        THIRTY("30대"), // 30대
        FORTY("40대"),  // 40대
        FIFTY_PLUS("50대 이상"); // 50대 이상

        private final String value;

        AgeGroup(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static AgeGroup fromValue(String value) {
            for (AgeGroup ageGroup : AgeGroup.values()) {
                if (ageGroup.getValue().equals(value)) {
                    return ageGroup;
                }
            }
            throw new IllegalArgumentException("Invalid age group provided.");
        }
    }
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AgeGroup userAgeGroup;


    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime userRegDate = LocalDateTime.now();

    private LocalDateTime userLoginDate;
    private LocalDateTime userLogoutDate;


    // 회원 상태 enum으로 관리
    public enum MemberStatus{
        ABLE, DELETED, SLEEP
    }
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private MemberStatus userStatus;

    @Builder.Default
    @Column(nullable = false)
    private Boolean userSocialTF = false;

    public enum UserRole {
        USER, ADMIN
    }

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role",nullable = false)
    private UserRole role = UserRole.USER;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> role.name());  // 권한을 GrantedAuthority로 변환
    }
    @ManyToMany
    @JoinTable(
            name = "user_tags",
            joinColumns = @JoinColumn(name = "user_number"),
            inverseJoinColumns = @JoinColumn(name = "tag_number")
    )
    private List<Tag> preferredTags;

    @Builder
    public Users(String userEmail, String userPw, String userName, Gender userGender, AgeGroup userAgeGroup, List<Tag> preferredTags) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userName = userName;
        this.userGender = userGender;
        this.userAgeGroup = userAgeGroup;
        this.preferredTags = (preferredTags != null) ? preferredTags : List.of(); // 태그가 없으면 빈 리스트
    }

    // 선호 태그 설정 메서드
    public void setPreferredTags(List<Tag> preferredTags) {
        this.preferredTags = preferredTags;
    }

    // Setters and Getters
    public void setPassword(String password) {
        this.userPw = password;
    }
    public String getEmail() {
        return this.userEmail;
    }
    public Integer getId() {
        return this.userNumber;
    }

    public boolean isAccountNonExpired() {
        return true;
    }


    public boolean isAccountNonLocked() {
        return true;
    }


    public boolean isCredentialsNonExpired() {
        return true;
    }


    public boolean isEnabled() {
        return true;
    }



}
