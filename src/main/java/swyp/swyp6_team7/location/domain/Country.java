package swyp.swyp6_team7.location.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
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
}
