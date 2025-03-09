package swyp.swyp6_team7.Plan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_plan_spot")
@Entity
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 연관 일정 식별자
    @Column(name = "plan_id", nullable = false)
    private Long planId;

    // 장소 순번 (1~10)
    @Column(name = "spot_order", nullable = false)
    private Integer order;

    // 장소 위도 좌표
    @Column(name = "spot_latitude", nullable = false, length = 15)
    private String latitude;

    // 장소 경도 좌표
    @Column(name = "spot_longitude", nullable = false, length = 15)
    private String longitude;

    // 장소명
    @Column(name = "spot_name", nullable = false, length = 30)
    private String name;

    // 장소 카테고리
    @Column(name = "spot_category", length = 15)
    private String category;

    // 장소 시도
    @Column(name = "spot_region", length = 15)
    private String region;

    @Builder
    public Spot(
            Long id, Long planId, Integer order, String latitude, String longitude,
            String name, String category, String region
    ) {
        this.id = id;
        this.planId = planId;
        this.order = order;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.category = category;
        this.region = region;
    }

    public static Spot create(
            Long planId, Integer order, String latitude, String longitude,
            String name, String category, String region
    ) {
        return Spot.builder()
                .planId(planId)
                .order(order)
                .latitude(latitude)
                .longitude(longitude)
                .name(name)
                .category(category)
                .region(region)
                .build();
    }

    @Override
    public String toString() {
        return "Spot{" +
                "id=" + id +
                ", planId=" + planId +
                ", order=" + order +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
