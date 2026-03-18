package ws.furrify.testcore.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
public class PostgresTestConfig {
    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withInitScript("db/init.sql");
}
