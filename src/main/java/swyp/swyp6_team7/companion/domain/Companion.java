package swyp.swyp6_team7.companion.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import swyp.swyp6_team7.travel.domain.Travel;

@Getter
@Table(name = "travel_companion", uniqueConstraints = {
        @UniqueConstraint(
                name = "travel_companion_unique", columnNames = {"travel_number", "user_number"}
        )}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Companion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_number", nullable = false)
    private Travel travel;

    @Column(name = "user_number", nullable = false)
    private int userNumber;


    @Builder
    public Companion(long number, Travel travel, int userNumber) {
        this.number = number;
        setTravel(travel);
        this.userNumber = userNumber;
    }

    private void setTravel(Travel travel) {
        this.travel = travel;
        travel.getCompanions().add(this);
    }

}