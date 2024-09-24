package swyp.swyp6_team7.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_loginhistories")
public class UserLoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int hisNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_number", nullable = false)
    private Users user;  // Users 테이블과의 관계 설정

    @Column(name = "his_login_date", nullable = false)
    private LocalDateTime hisLoginDate;

    @Column(name = "his_logout_date")
    private LocalDateTime hisLogoutDate;
}
