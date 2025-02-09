package swyp.swyp6_team7.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@Testcontainers
public class RedisContainerConfig {

    private static final String REDIS_DOCKER_IMAGE = "redis:6.2-alpine";
    private static final int REDIS_PORT = 6379;

    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer redisContainer() {
        return new GenericContainer<>(DockerImageName.parse(REDIS_DOCKER_IMAGE))
                .withExposedPorts(REDIS_PORT)
                .waitingFor(Wait.forListeningPort());
    }
}
