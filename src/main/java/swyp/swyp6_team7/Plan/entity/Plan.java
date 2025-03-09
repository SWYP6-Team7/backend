package swyp.swyp6_team7.Plan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_plan", uniqueConstraints = {
        @UniqueConstraint(
                name = "plan_unique", columnNames = {"travel_number", "plan_order"}
        )}
)
@Entity
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 연관 여행 식별자
    @Column(name = "travel_number", nullable = false)
    private Integer travelNumber;

    // 일정 순번(day 1 ~ 90)
    @Column(name = "plan_order", nullable = false)
    private Integer order;


    @Builder
    public Plan(Long id, Integer travelNumber, Integer order) {
        this.id = id;
        this.travelNumber = travelNumber;
        this.order = order;
    }

    public static Plan create(Integer travelNumber, Integer order) {
        return Plan.builder()
                .travelNumber(travelNumber)
                .order(order)
                .build();
    }

    @Override
    public String toString() {
        return "Plan{" +
                "id=" + id +
                ", travelNumber=" + travelNumber +
                ", order=" + order +
                '}';
    }
}
