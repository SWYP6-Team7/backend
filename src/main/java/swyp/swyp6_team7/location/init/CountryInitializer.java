package swyp.swyp6_team7.location.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.location.dao.CountryDao;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.parser.CountryParser;
import swyp.swyp6_team7.location.reader.CsvReader;

import java.io.InputStream;
import java.util.List;


@Component
@RequiredArgsConstructor
public class CountryInitializer {
    private final CsvReader<Country> csvReader;
    private final CountryParser countryParser;
    private final CountryDao countryDao;

    @PostConstruct
    public void initCountries() {
        try {
            InputStream input = new ClassPathResource("cities/countries.csv").getInputStream();
            List<Country> countries = csvReader.readByLine(input, countryParser, null);
            countries.stream()
                    .filter(country -> country != null)
                    .forEach(countryDao::addCountry);
            System.out.println("countries.csv 적재 완료 (" + countries.size() + "건)");
        } catch (Exception e) {
            System.err.println("countries.csv 적재 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
