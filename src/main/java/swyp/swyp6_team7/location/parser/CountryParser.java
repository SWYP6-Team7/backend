package swyp.swyp6_team7.location.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.location.dao.CountryDao;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.domain.LocationType;

@Component
@RequiredArgsConstructor
public class CountryParser implements Parser<Country> {
    private final CountryDao countryDao;

    @Override
    public Country parse(String line, LocationType locationType) {
        String[] parts = line.split(",");
        if (parts.length < 4) return null;

        String countryName = parts[0].trim();
        double latitude = Double.parseDouble(parts[1].trim());
        double longitude = Double.parseDouble(parts[2].trim());
        String continentStr = parts[3].trim();

        if (countryDao.isCountryExists(countryName)) return null;

        Continent continent = Continent.fromString(continentStr);

        Country country = new Country();
        country.setCountryName(countryName);
        country.setLatitude(latitude);
        country.setLongitude(longitude);
        country.setContinent(continent);

        return country;
    }
}
