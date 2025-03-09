package swyp.swyp6_team7.location.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.location.dao.LocationDao;
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

    @Override
    public Location parse(String line, LocationType locationType) {
        String[] columns = line.split(",");

        String location;

        switch (locationType) {
            case DOMESTIC:
                location = columns[3].trim(); // 소분류 (시)
                // '시' 제거 로직 추가
                if (location.endsWith("시")) {
                    location = location.substring(0, location.length() - 1);
                }
                break;

            case INTERNATIONAL:
                location = columns[2].trim(); // 중분류 (나라)

                // 중복 체크 로직 추가
                if (locationDao.isLocationExists(location, locationType)) {
                    // 이미 해당 나라가 DB에 있는 경우, 중복 추가 방지
                    System.out.println("Location already exists: " + location);
                    return null; // null 반환으로 추가하지 않음
                }
                break;

            default:
                throw new IllegalArgumentException("지원되지 않는 LocationType입니다: " + locationType);
        }

        return new Location(location, locationType);
    }

    public void parseAndSave(String resourcePath, LocationType locationType) {
        Resource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            br.readLine(); // 첫 번째 행 건너뜀 (헤더)
            while ((line = br.readLine()) != null) {
                Location locationObject = parse(line, locationType);
                if (locationObject != null) {
                    locationDao.addCity(locationObject);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
