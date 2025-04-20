package swyp.swyp6_team7.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.location.dao.LocationDao;
import swyp.swyp6_team7.location.domain.Country;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.parser.CityParser;
import swyp.swyp6_team7.location.reader.CsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final CsvReader<Location> csvReader;
    private final LocationDao locationDao;
    private final CityParser cityParser;


    public void importCities(InputStream inputStream, LocationType locationType) throws IOException {
        List<Location> cities = csvReader.readByLine(inputStream, cityParser, locationType);
        System.out.println("location 데이터 적재 완료: " + cities.size() + "건");
    }

    public void loadAllLocations() throws IOException {
        // ClassPathResource를 사용하여 InputStream을 가져옵니다.
        try (InputStream domesticStream = new ClassPathResource("cities/korea_cities.csv").getInputStream();
             InputStream internationalStream = new ClassPathResource("cities/foreign_cities.csv").getInputStream()) {

            importCities(domesticStream, LocationType.DOMESTIC);
            importCities(internationalStream, LocationType.INTERNATIONAL);
        }
    }
}
