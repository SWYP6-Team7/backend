package swyp.swyp6_team7.enrollment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Table(name = "enrollments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_number")
    private Long number;

    @Column(name = "user_number", nullable = false)
    private Integer userNumber;

    @Column(name = "travel_number", nullable = false)
    private Integer travelNumber;

    @CreatedDate
    @Column(name = "enrollment_datetime", nullable = false)
    private LocalDateTime createdAt;

    @Lob
    @Column(name = "enrollment_message", length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false)
    private EnrollmentStatus status;


    @Builder
    public Enrollment(
            long number, int userNumber, int travelNumber,
            LocalDateTime createdAt, String message, EnrollmentStatus status
    ) {
        this.number = number;
        this.userNumber = userNumber;
        this.travelNumber = travelNumber;
        this.createdAt = createdAt;
        this.message = message;
        this.status = status;
    }

    public static Enrollment create(int userNumber, int travelNumber, String message) {
        return Enrollment.builder()
                .userNumber(userNumber)
                .travelNumber(travelNumber)
                .message(message)
                .status(EnrollmentStatus.PENDING)
                .build();
    }

    public void accepted() {
        this.status = EnrollmentStatus.ACCEPTED;
    }

    public void rejected() {
        this.status = EnrollmentStatus.REJECTED;
    }

}
