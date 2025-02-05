package swyp.swyp6_team7.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;


public class RedisContainerConfig implements BeforeAllCallback {

    private static final String REDIS_DOCKER_IMAGE = "redis:6.2-alpine";
    private static final int REDIS_PORT = 6379;
    private static GenericContainer redisContainer;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        redisContainer = new GenericContainer<>(DockerImageName.parse(REDIS_DOCKER_IMAGE))
                .withExposedPorts(REDIS_PORT)
                .waitingFor(Wait.forListeningPort());

        redisContainer.start();

        System.setProperty("redis.host", redisContainer.getHost());
        System.setProperty("redis.port", redisContainer.getMappedPort(REDIS_PORT).toString());
    }
}
