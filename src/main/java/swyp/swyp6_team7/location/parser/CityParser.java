package swyp.swyp6_team7.location.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.location.dao.CountryDao;
import swyp.swyp6_team7.location.dao.LocationDao;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
@RequiredArgsConstructor
public class CityParser implements Parser<Location> {

    private final LocationDao locationDao;
    private final CountryDao countryDao;

    @Override
    public Location parse(String line, LocationType locationType) {
        // foreign: id,continent,country,city,city_en
        // korea: id,country,region,city

        String[] columns = line.split(",");
        if (columns.length < 4) return null;

        String countryName;
        String cityName;
        Continent continent = Continent.ASIA; // 기본값 국내 데이터 파싱에 사용

        if (locationType == LocationType.DOMESTIC) {
            countryName = columns[1].trim(); // 대한민국
            cityName = columns[3].trim().replace("시", ""); // 서울시 → 서울
        } else {
            countryName = columns[2].trim(); // 예: 미국
            cityName = columns[3].trim(); // 예: 뉴욕
            try {
                continent = Continent.fromString(columns[1].trim());
            } catch (Exception ignored) {
                // 유럽/아시아 같은 애매한 값 무시
            }
        }

        final Continent finalContinent = continent;

        // country 조회 또는 신규 생성
        Country country = countryDao.findByCountryName(countryName)
                .orElseGet(() -> countryDao.insertCountry(countryName, finalContinent));

        locationDao.updateCountryIdForMatchingLocationName(country.getCountryName(), country.getId());

        // 이미 존재하면 업데이트, 없으면 삽입
        if (locationDao.existsByLocationName(cityName)) {
            locationDao.updateLocationWithCountry(cityName, country.getCountryName(), country.getId());
        } else {
            Location location = Location.builder()
                    .locationName(cityName)
                    .locationType(locationType)
                    .country(country)
                    .build();
            locationDao.addCity(location, country);
        }
        return null;

    }
}
