package swyp.swyp6_team7.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelViewCountService {

    /**
     * 조회수 key = view:travel:{travelNumber}, value = String type 조회수<br>
     * 조회여부 key = viewed:travel:{travelNumber}, value = Set of userIdentifier
     */
    private static final String VIEW_COUNT_KEY_PREFIX = "view:travel:";
    private static final String VIEWED_INFO_KEY_PREFIX = "viewed:travel:";

    private final RedisTemplate<String, String> redisTemplate;
    private final TravelRepository travelRepository;

    // 여행 조회수 update
    public void updateViewCount(Integer travelNumber, String userIdentifier) {
        // userIdentifier 예외 처리
        if (userIdentifier == null || userIdentifier.isBlank()) {
            log.warn("Invalid userIdentifier: userIdentifier={}", userIdentifier);
            throw new IllegalArgumentException("잘못된 user Identifier 입니다.");
        }

        String viewedInfoKey = VIEWED_INFO_KEY_PREFIX + travelNumber.toString();
        if (!isViewed(viewedInfoKey, userIdentifier)) {
            addUserIdentifierToViewedSet(viewedInfoKey, userIdentifier); // viewed set에 userIdentifier 추가
            increaseViewCount(travelNumber); // 조회수 증가
        }
    }

    // 조회 여부 확인
    private boolean isViewed(String viewedInfoKey, String userIdentifier) {
        return redisTemplate.opsForSet().isMember(viewedInfoKey, userIdentifier);
    }

    // 특정 여행 조회수 조회
    public Integer getViewCount(Integer travelNumber) {
        String key = VIEW_COUNT_KEY_PREFIX + travelNumber.toString();
        String viewCount = redisTemplate.opsForValue().get(key);
        return viewCount != null ? Integer.valueOf(viewCount) : 0;
    }

    // 사용자 조회 정보 추가
    private void addUserIdentifierToViewedSet(String viewedInfoKey, String userIdentifier) {
        redisTemplate.opsForSet().add(viewedInfoKey, userIdentifier);
        redisTemplate.expire(viewedInfoKey, 24, TimeUnit.HOURS); // TTL 1일
        log.debug("Redis: 사용자 조회 정보 추가: viewedInfoKey={}, userIdentifier={}", viewedInfoKey, userIdentifier);
    }

    // 특정 여행 조회수 1 증가
    private void increaseViewCount(Integer travelNumber) {
        String key = VIEW_COUNT_KEY_PREFIX + travelNumber.toString();

        String viewCount = redisTemplate.opsForValue().get(key);
        if (viewCount == null) {
            redisTemplate.opsForValue().set(key, "0");
            log.info("Redis: Set new viewCount data: key={}", key);
        }
        redisTemplate.opsForValue().increment(key);
    }

    // 조회 사용자 기록 삭제 작업 (매일 새벽 4시 실행)
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void deleteViewInfo() {
        log.info("SCHEDULER::DAILY: Redis 사용자 조회 기록 삭제 작업 시작");

        Set<String> keys = redisTemplate.keys(VIEWED_INFO_KEY_PREFIX + "*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Redis: Delete viewInfo data");
        }

        log.info("SCHEDULER::DAILY: Redis 사용자 조회 기록 삭제 작업 종료");
    }

    // 조회수 DB 동기화 작업 (10분 주기 실행)
    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void combineViewCountToDatabase() {
        log.info("SCHEDULER::10MIN: Redis-DB 조회수 동기화 작업 시작");

        Set<String> keys = redisTemplate.keys(VIEW_COUNT_KEY_PREFIX + "*");
        if (!keys.isEmpty()) {
            Map<Integer, Integer> viewCountMap = getViewCountData(keys);
            log.info("viewCountMap: {}", viewCountMap);

            redisTemplate.delete(keys);
            log.info("Redis: Delete All viewCount data");

            for (Integer travelNumber : viewCountMap.keySet()) {
                travelRepository.combineViewCountByTravelNumber(travelNumber, viewCountMap.get(travelNumber));
                log.info("Combine viewCount to database: travelNumber={}", travelNumber);
            }
        }

        log.info("SCHEDULER::10MIN: Redis-DB 조회수 동기화 작업 종료");
    }

    private Map<Integer, Integer> getViewCountData(Set<String> redisKeys) {
        Map<Integer, Integer> viewCountMap = new HashMap<>(); // key: travelNumber, value: viewCount
        redisKeys.stream()
                .forEach(redisKey -> {
                    viewCountMap.put(
                            Integer.parseInt(redisKey.split(":")[2]),
                            Integer.valueOf(redisTemplate.opsForValue().get(redisKey))
                    );
                });

        return viewCountMap;
    }

}
