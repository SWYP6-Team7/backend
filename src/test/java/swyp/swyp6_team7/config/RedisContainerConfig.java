package swyp.swyp6_team7.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration
@Testcontainers
public class RedisContainerConfig {

    private static final String REDIS_DOCKER_IMAGE = "redis:6.2-alpine";
    private static final int REDIS_PORT = 6379;

    private static final GenericContainer<?> container;

    static {
        container = new GenericContainer<>("redis:latest")
                .withExposedPorts(REDIS_PORT);
        container.start();
    }

    @Primary
    @Bean
    public LettuceConnectionFactory testRedisConnectionFactory() {
        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(
                        container.getHost(),
                        container.getMappedPort(REDIS_PORT)
                )
        );
    }
}
