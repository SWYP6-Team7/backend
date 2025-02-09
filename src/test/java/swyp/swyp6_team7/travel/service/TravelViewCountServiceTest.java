package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import swyp.swyp6_team7.config.RedisContainerConfig;
import swyp.swyp6_team7.location.domain.Location;
import swyp.swyp6_team7.location.domain.LocationType;
import swyp.swyp6_team7.location.repository.LocationRepository;
import swyp.swyp6_team7.travel.domain.GenderType;
import swyp.swyp6_team7.travel.domain.PeriodType;
import swyp.swyp6_team7.travel.domain.Travel;
import swyp.swyp6_team7.travel.domain.TravelStatus;
import swyp.swyp6_team7.travel.repository.TravelRepository;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Import(RedisContainerConfig.class)
@SpringBootTest
class TravelViewCountServiceTest {

    private static final String VIEW_COUNT_KEY_PREFIX = "view:travel:";
    private static final String VIEWED_INFO_KEY_PREFIX = "viewed:travel:";

    @Autowired
    private TravelViewCountService travelViewCountService;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void tearDown() {
        travelRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
        redisTemplate.delete(redisTemplate.keys(VIEW_COUNT_KEY_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(VIEWED_INFO_KEY_PREFIX + "*"));
    }

    @DisplayName("updateViewCount: 24시간 내에 처음 접속하면 Redis의 여행 조회수가 1 증가하고 사용자 기록이 저장된다.")
    @Test
    void updateViewCount() {
        // given
        Integer travelNumber = 10;
        String userIdentifier = "5";

        String key = VIEW_COUNT_KEY_PREFIX + travelNumber.toString();
        redisTemplate.opsForValue().set(key, "3"); // 조회수 3으로 설정

        // when
        travelViewCountService.updateViewCount(travelNumber, userIdentifier);

        // then
        String viewCount = redisTemplate.opsForValue().get(VIEW_COUNT_KEY_PREFIX + travelNumber.toString());
        assertThat(Integer.parseInt(viewCount)).isEqualTo(4);

        Boolean isViewed = redisTemplate.opsForSet().isMember(VIEWED_INFO_KEY_PREFIX + travelNumber, userIdentifier);
        assertThat(isViewed).isEqualTo(true);
    }

    @DisplayName("updateViewCount: 24시간 내에 접속한 기록이 있을 경우, Redis의 여행 조회수는 변하지 않는다.")
    @Test
    void updateViewCountWhenAlreadyViewed() {
        // given
        Integer travelNumber = 10;
        String userIdentifier = "5";

        String key = VIEW_COUNT_KEY_PREFIX + travelNumber.toString();
        redisTemplate.opsForValue().set(key, "3"); // 조회수 3으로 설정

        String viewedInfoKey = VIEWED_INFO_KEY_PREFIX + travelNumber.toString();
        redisTemplate.opsForSet().add(viewedInfoKey, userIdentifier); // viewed에 사용자 정보 추가
        redisTemplate.expire(viewedInfoKey, 24, TimeUnit.HOURS);

        // when
        travelViewCountService.updateViewCount(travelNumber, userIdentifier);

        // then
        String viewCount = redisTemplate.opsForValue().get(VIEW_COUNT_KEY_PREFIX + travelNumber.toString());
        assertThat(Integer.parseInt(viewCount)).isEqualTo(3);
    }

    @DisplayName("updateViewCount: 여러 사용자가 동시에 조회했을 때 요청횟수만큼 조회수가 증가한다.")
    @Test
    void updateViewCountWhenManyUser() throws InterruptedException {
        // given
        Integer travelNumber = 10;
        String key = VIEW_COUNT_KEY_PREFIX + travelNumber.toString();
        redisTemplate.opsForValue().set(key, "0"); // 조회수 0으로 설정

        int userCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(userCount);

        // when
        for (int i = 1; i <= userCount; i++) {
            String userIdentifier = String.valueOf(i);
            executorService.submit(() -> {
                try {
                    travelViewCountService.updateViewCount(travelNumber, userIdentifier);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        String viewCount = redisTemplate.opsForValue().get(VIEW_COUNT_KEY_PREFIX + travelNumber.toString());
        assertThat(Integer.parseInt(viewCount)).isEqualTo(10);
    }

    @DisplayName("deleteViewInfo: Redis에 저장된 사용자 조회 기록을 삭제한다.")
    @Test
    void deleteViewInfo() {
        // given
        Integer travelNumber1 = 10;
        Integer travelNumber2 = 11;
        String userIdentifier = "123";

        redisTemplate.opsForSet().add(VIEWED_INFO_KEY_PREFIX + travelNumber1, userIdentifier);
        redisTemplate.expire(VIEWED_INFO_KEY_PREFIX + travelNumber1, 24, TimeUnit.HOURS);

        redisTemplate.opsForSet().add(VIEWED_INFO_KEY_PREFIX + travelNumber2, userIdentifier);
        redisTemplate.expire(VIEWED_INFO_KEY_PREFIX + travelNumber2, 24, TimeUnit.HOURS);

        // when
        travelViewCountService.deleteViewInfo();

        // then
        Set<String> keys = redisTemplate.keys(VIEWED_INFO_KEY_PREFIX + "*");
        assertThat(keys).isEmpty();
    }

    @DisplayName("deleteViewInfo: 해당 메서드는 매일 새벽 4시마다 실행된다.")
    @Test
    void deleteViewInfoScheduler() {
        // given
        String methodName = "deleteViewInfo";
        CronTrigger trigger = getTriggerFromMethod(TravelViewCountService.class, methodName);

        LocalDateTime initialTime = LocalDateTime.of(2025, 1, 23, 0, 0);
        Instant instant = toInstant(initialTime);
        SimpleTriggerContext context = new SimpleTriggerContext(instant, instant, instant);

        List<Instant> expectedTimes = List.of(
                LocalDateTime.of(2025, 1, 23, 4, 0),
                LocalDateTime.of(2025, 1, 24, 4, 0),
                LocalDateTime.of(2025, 1, 25, 4, 0)
        ).stream().map(this::toInstant).toList();

        // when // then
        for (Instant expectedTime : expectedTimes) {
            Instant actual = trigger.nextExecution(context);
            assertThat(actual).isEqualTo(expectedTime);
            context.update(actual, actual, actual);
        }
    }

    @DisplayName("combineViewCountToDatabase: Redis의 조회수를 DB에 합산한다.")
    @Test
    void combineViewCountToDatabase() {
        // given
        Location location = locationRepository.save(createLocation());
        Travel travel1 = travelRepository.save(createTravel(5, location));
        Travel travel2 = travelRepository.save(createTravel(7, location));

        redisTemplate.opsForValue().set(VIEW_COUNT_KEY_PREFIX + travel1.getNumber(), "10");
        redisTemplate.opsForValue().set(VIEW_COUNT_KEY_PREFIX + travel2.getNumber(), "20");

        // when
        travelViewCountService.combineViewCountToDatabase();

        // then
        assertThat(travelRepository.findAll()).hasSize(2)
                .extracting("number", "viewCount")
                .containsExactlyInAnyOrder(
                        tuple(travel1.getNumber(), 15),
                        tuple(travel2.getNumber(), 27)
                );
        assertThat(redisTemplate.keys(VIEW_COUNT_KEY_PREFIX + "*")).isEmpty();
    }

    @DisplayName("combineViewCountToDatabase: 해당 메서드는 10분마다 실행된다.")
    @Test
    void combineViewCountToDatabaseScheduler() {
        // given
        String methodName = "combineViewCountToDatabase";
        CronTrigger trigger = getTriggerFromMethod(TravelViewCountService.class, methodName);

        LocalDateTime initialTime = LocalDateTime.of(2025, 1, 23, 0, 0);
        Instant instant = toInstant(initialTime);
        SimpleTriggerContext context = new SimpleTriggerContext(instant, instant, instant);

        List<Instant> expectedTimes = List.of(
                LocalDateTime.of(2025, 1, 23, 0, 10),
                LocalDateTime.of(2025, 1, 23, 0, 20),
                LocalDateTime.of(2025, 1, 23, 0, 30)
        ).stream().map(this::toInstant).toList();

        // when // then
        for (Instant expectedTime : expectedTimes) {
            Instant actual = trigger.nextExecution(context);
            assertThat(actual).isEqualTo(expectedTime);
            context.update(actual, actual, actual);
        }
    }


    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    private CronTrigger getTriggerFromMethod(Class<?> targetClass, String methodName) {
        Method method = ReflectionUtils.findMethod(targetClass, methodName).get();
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        return createTrigger(scheduled);
    }

    private CronTrigger createTrigger(Scheduled scheduled) {
        if (StringUtils.isNotBlank(scheduled.zone())) {
            return new CronTrigger(scheduled.cron(), TimeZone.getTimeZone(scheduled.zone()));
        } else {
            return new CronTrigger(scheduled.cron());
        }
    }

    private Location createLocation() {
        return Location.builder()
                .locationName("Seoul")
                .locationType(LocationType.DOMESTIC)
                .build();
    }

    private Travel createTravel(int viewCount, Location location) {
        return Travel.builder()
                .userNumber(1)
                .location(location)
                .locationName(location.getLocationName())
                .title("여행 제목")
                .details("여행 내용")
                .viewCount(viewCount)
                .maxPerson(2)
                .genderType(GenderType.MIXED)
                .periodType(PeriodType.ONE_WEEK)
                .status(TravelStatus.IN_PROGRESS)
                .enrollmentsLastViewedAt(LocalDateTime.of(2024, 11, 17, 12, 0))
                .build();
    }
}
