package javasabr.mqtt.auth.service.config;

import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "persistence.database")
public record DatabaseConnectionProperties(
    Map<String, Credentials> users,
    String driver,
    String host,
    int port,
    String name,
    String credentialsQuery,
    Duration maxIdleTime,
    int initialPoolSize,
    int maxPoolSize,
    String lockTimeout,
    String statementTimeout
) implements DatabasePoolConfig, DatabaseUrlConfig, DatabaseUsersConfig, DatabaseTimeoutsConfig {}
