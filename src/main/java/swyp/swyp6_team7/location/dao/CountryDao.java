package swyp.swyp6_team7.location.dao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.domain.Continent;

import java.util.Optional;

@Repository
public class CountryDao {
    private final JdbcTemplate jdbcTemplate;

    public CountryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addCountry(Country country) {
        if (isCountryExists(country.getCountryName())) {
            System.out.println("Duplicate country: " + country.getCountryName());
            return;
        }

        String sql = "INSERT INTO countries (country_name, latitude, longitude, continent) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql,
                    country.getCountryName(),
                    country.getLatitude(),
                    country.getLongitude(),
                    country.getContinent().name());
        } catch (DuplicateKeyException e) {
            System.out.println("Already exists: " + country.getCountryName());
        }
    }

    public boolean isCountryExists(String countryName) {
        String sql = "SELECT COUNT(*) FROM countries WHERE country_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{countryName}, Integer.class);
        return count != null && count > 0;
    }

    public Optional<Country> findByCountryName(String countryName) {
        String sql = "SELECT * FROM countries WHERE country_name = ?";
        return jdbcTemplate.query(sql, new Object[]{countryName}, rs -> {
            if (rs.next()) {
                Country country = new Country();
                country.setId(rs.getInt("country_id"));
                country.setCountryName(rs.getString("country_name"));
                country.setLatitude(rs.getDouble("latitude"));
                country.setLongitude(rs.getDouble("longitude"));
                country.setContinent(Continent.valueOf(rs.getString("continent")));
                return Optional.of(country);
            }
            return Optional.empty();
        });
    }

    public Country insertCountry(String countryName, Continent continent) {
        String sql = "INSERT INTO countries (country_name, continent) VALUES (?, ?)";
        jdbcTemplate.update(sql, countryName, continent.name());

        return findByCountryName(countryName).orElseThrow(() ->
                new IllegalStateException("Country insert failed: " + countryName));
    }

}
