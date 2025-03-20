package swyp.swyp6_team7.location.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.repository.LocationRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class LocationAutocompleteService {
    private final LocationRepository locationRepository;
    private final RedisTemplate<String, List<String>> redisTemplate;

    public LocationAutocompleteService(LocationRepository locationRepository, RedisTemplate<String, List<String>> redisTemplate) {
        this.locationRepository = locationRepository;
        this.redisTemplate = redisTemplate;
    }

    public List<String> getAutocompleteSuggestions(String prefix) {
        // 현재 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userNumber = authentication.getName();

        // 사용자별 캐시 키 생성
        String cacheKey = userNumber + ":" + prefix;


        // Redis 캐시에서 데이터 조회
        List<String> cachedSuggestions = redisTemplate.opsForValue().get(cacheKey);

        if (cachedSuggestions == null) {
            // 캐시에 없으면 DB에서 검색
            // 캐시에 없으면 DB에서 검색
            List<String> newSuggestions = locationRepository.findByLocationNameStartingWith(prefix)
                    .stream()
                    .map(Location::getLocationName)
                    .limit(5)  // 결과 수 제한
                    .collect(Collectors.toList());

            // 비동기적으로 캐시 업데이트
            CompletableFuture.runAsync(() -> redisTemplate.opsForValue().set(cacheKey, newSuggestions));


            return newSuggestions;
        }

        return cachedSuggestions;
    }

}
