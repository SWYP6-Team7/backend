package swyp.swyp6_team7.location.dao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.domain.Continent;

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
}
