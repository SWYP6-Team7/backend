package swyp.swyp6_team7.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.location.domain.Continent;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.repository.CountryRepository;

@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository countryRepository;

    @Transactional
    public Country InsertCountry(String countryName, Continent defaultContinent) {
        return countryRepository.findByCountryName(countryName)
                .orElseGet(() -> {
                    Country country = new Country();
                    country.setCountryName(countryName);
                    country.setContinent(defaultContinent);
                    country.setLatitude(null); // 후처리 가능
                    country.setLongitude(null);
                    return countryRepository.save(country);
                });
    }
}
