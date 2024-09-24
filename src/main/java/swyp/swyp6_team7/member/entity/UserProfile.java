package swyp.swyp6_team7.member.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer proNumber;

    @Column(nullable = false)
    private Integer userNumber;

    @Column(length = 200)
    private String proIntroduce;

    private String profileImageUrl;  // 프로필 이미지 URL

    // Getters and Setters
    public Integer getProNumber() {
        return proNumber;
    }

    public void setProNumber(Integer proNumber) {
        this.proNumber = proNumber;
    }

    public Integer getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(Integer userNumber) {
        this.userNumber = userNumber;
    }

    public String getProIntroduce() {
        return proIntroduce;
    }

    public void setProIntroduce(String proIntroduce) {
        this.proIntroduce = proIntroduce;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
