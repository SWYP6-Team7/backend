package swyp.swyp6_team7.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@Testcontainers
public class RedisContainerConfig {

    private static final int REDIS_PORT = 6379;

    @Bean
    public GenericContainer<?> redisContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(REDIS_PORT);
        container.start();
        System.out.println("Redis TestContainer started at: " + container.getHost() + ":" + container.getMappedPort(REDIS_PORT));
        return container;
    }

    @Primary
    @Bean
    public LettuceConnectionFactory testRedisConnectionFactory(GenericContainer<?> redisContainer) {
        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(
                        redisContainer.getHost(),
                        redisContainer.getMappedPort(REDIS_PORT)
                )
        );
    }
}
