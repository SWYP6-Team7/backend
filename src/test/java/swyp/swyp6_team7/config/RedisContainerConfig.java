package swyp.swyp6_team7.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration
@Testcontainers
public class RedisContainerConfig {

    private static final int REDIS_PORT = 6379;

    private static final GenericContainer<?> container;

    static {
        container = new GenericContainer<>("redis:latest")
                .withExposedPorts(REDIS_PORT);
        container.start();
    }
}
