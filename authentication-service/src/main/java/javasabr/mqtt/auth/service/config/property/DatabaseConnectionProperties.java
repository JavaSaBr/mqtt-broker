package javasabr.mqtt.auth.service.config.property;

import java.time.Duration;
import java.util.Map;
import javasabr.mqtt.auth.service.config.DatabaseUsersConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "persistence.database")
public record DatabaseConnectionProperties(
    Map<String, DatabaseCredentials> users,
    DatabaseDriver dbDriver,
    String dbHost,
    int dbPort,
    String dbName,
    String credentialsQuery,
    Duration maxIdleTime,
    int initialPoolSize,
    int maxPoolSize,
    String lockTimeout,
    String statementTimeout
) implements DatabasePoolConfig, DatabaseUrlConfig, DatabaseUsersConfig, DatabaseTimeoutsConfig {}
