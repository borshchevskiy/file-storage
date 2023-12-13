package ru.borshchevskiy.filestorage.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.time.Duration;


public abstract class IntegrationTestBase {

    private static final String accessKey = "MINIO";
    private static final String secretKey = "MINIOMINIO";

    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.3")
            .withInitScript("db/scripts/init-schema.sql")
            .withDatabaseName("file_storage")
            .withUrlParam("currentSchema", "file_storage");

    private static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);


    private static GenericContainer<?> minioContainer = new GenericContainer<>("minio/minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", accessKey)
            .withEnv("MINIO_ROOT_PASSWORD", secretKey)
            .withCommand("server /data --address :9000")
            .waitingFor(new HttpWaitStrategy()
                    .forPort(9000)
                    .forPath("/minio/health/ready")
                    .withStartupTimeout(Duration.ofSeconds(10)));

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
        redisContainer.start();
        minioContainer.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        String minioUrl = "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000);
        registry.add("minio.url", () -> minioUrl);
    }

}
