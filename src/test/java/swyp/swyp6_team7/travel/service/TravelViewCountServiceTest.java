package swyp.swyp6_team7.travel.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TravelViewCountServiceTest {

    @Autowired
    private TravelViewCountService travelViewCountService;


    @DisplayName("deleteViewInfo: 해당 메서드는 매일 새벽 4시마다 실행된다.")
    @Test
    void deleteViewInfo() {
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

    @DisplayName("combineViewCountToDatabase: 해당 메서드는 10분마다 실행된다.")
    @Test
    void combineViewCountToDatabase() {
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

}