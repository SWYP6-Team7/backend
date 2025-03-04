package swyp.swyp6_team7.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import swyp.swyp6_team7.tag.domain.Tag;
import swyp.swyp6_team7.tag.domain.UserTagPreference;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@Getter
//@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
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

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Gender userGender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AgeGroup userAgeGroup;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime userRegDate = LocalDateTime.now();

    private LocalDateTime userLoginDate;
    private LocalDateTime userLogoutDate;


    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Builder.Default
    @Column(name = "user_social_TF", nullable = false)
    private Boolean userSocialTF = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role = UserRole.USER;
    @ManyToMany
    @JoinTable(
            name = "user_tags",
            joinColumns = @JoinColumn(name = "user_number"),
            inverseJoinColumns = @JoinColumn(name = "tag_number")
    )
    private Set<Tag> preferredTags;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserTagPreference> tagPreferences;  // user_tagpreferences 참조

    @Builder
    public Users(Integer userNumber, String userEmail, String userPw, String userName, Gender userGender, AgeGroup userAgeGroup, Set<Tag> preferredTags) {
        this.userNumber = userNumber;
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userName = userName;
        this.userGender = userGender;
        this.userAgeGroup = userAgeGroup;
        this.preferredTags = (preferredTags != null) ? preferredTags : Set.of(); // 태그가 없으면 빈 리스트
    }

    @Builder
    public Users(String userEmail, String userPw, String userName, Gender userGender, AgeGroup userAgeGroup, Set<Tag> preferredTags) {
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userName = userName;
        this.userGender = userGender;
        this.userAgeGroup = userAgeGroup;
        this.preferredTags = (preferredTags != null) ? preferredTags : Set.of(); // 태그가 없으면 빈 리스트
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> role.name());  // 권한을 GrantedAuthority로 변환
    }

    // 선호 태그 설정 메서드
    public void setPreferredTags(Set<Tag> preferredTags) {
        this.preferredTags = preferredTags;
    }

    public void setPassword(String password) {
        this.userPw = password;
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

    public boolean isBlocked() {
        return userStatus == UserStatus.BLOCK;
    }

}
