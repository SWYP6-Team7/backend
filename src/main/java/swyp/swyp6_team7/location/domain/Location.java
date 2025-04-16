package swyp.swyp6_team7.location.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "locations", indexes = {
        @Index(name = "idx_location_name", columnList = "location_name")})
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;
    @Column(name = "location_name",unique = true)
    private String locationName;
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type")
    private LocationType locationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;



    @Builder
    public Location(String locationName,LocationType locationType) {
        this.locationName = locationName;
        this.locationType = locationType;
    }
}
