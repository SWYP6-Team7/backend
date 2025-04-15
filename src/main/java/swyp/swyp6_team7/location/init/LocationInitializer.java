package swyp.swyp6_team7.location.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.location.service.LocationService;

@Component
@RequiredArgsConstructor
public class LocationInitializer {
    private final LocationService locationService;

    @PostConstruct
    public void initLocations() {
        try {
            locationService.loadAllLocations();
        } catch (Exception e) {
            System.err.println("Location 초기 데이터 적재 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
