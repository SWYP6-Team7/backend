package swyp.swyp6_team7.location.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name ="countries")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private Integer id;

    @Column(name = "country_name",nullable = false, unique = true)
    private String countryName;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private Continent continent;

    @Builder
    public Country(String countryName, Continent continent) {
        this.countryName = countryName;
        this.continent = continent;
    }
}
