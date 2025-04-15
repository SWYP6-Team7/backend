package swyp.swyp6_team7.location.dao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;

@Repository
public class LocationDao {
    private final JdbcTemplate jdbcTemplate;

    public LocationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addCity(Location location, Country country) {
        String sql = "INSERT INTO locations (location_name, location_type, country_id) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql,
                    location.getLocationName(),
                    location.getLocationType().name(),
                    country.getId());
        } catch (DuplicateKeyException e) {
            System.out.println("중복으로 삽입 불가 : " + location.getLocationName());
        }
    }

    public boolean existsByLocationName(String locationName) {
        String sql = "SELECT COUNT(*) FROM locations WHERE location_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{locationName}, Integer.class);
        return count != null && count > 0;
    }

    public void updateLocationWithCountry(String locationName, String countryName, Integer countryId) {
        String locationType = countryName.equals("대한민국") ? "DOMESTIC" : "INTERNATIONAL";

        String sql = "UPDATE locations " +
                "SET country_id = ?, location_type = ? " +
                "WHERE location_name = ?";

        int rows = jdbcTemplate.update(sql, countryId, locationType, locationName);

        if (rows > 0) {
            System.out.println("✅ " + locationName + " 업데이트 완료 → country_id: " + countryId + ", type: " + locationType);
        } else {
            System.out.println("⚠️ " + locationName + " 은(는) 업데이트 대상 아님");
        }
    }

    public void updateCountryIdForMatchingLocationName(String countryName, Integer countryId) {
        String sql = "UPDATE locations SET country_id = ?, location_type = ? WHERE location_name = ?";
        String type = countryName.equals("대한민국") ? "DOMESTIC" : "INTERNATIONAL";

        int rows = jdbcTemplate.update(sql, countryId, type, countryName);
        if (rows > 0) {
            System.out.println("국가 location 업데이트됨 → " + countryName);
        }
    }

}
