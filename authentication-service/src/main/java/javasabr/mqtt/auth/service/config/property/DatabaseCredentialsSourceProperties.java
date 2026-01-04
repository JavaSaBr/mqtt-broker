package javasabr.mqtt.auth.service.config.property;

import java.time.Duration;
import javasabr.mqtt.auth.api.DatabaseDriver;
import javasabr.mqtt.auth.api.DatabaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("authentication.credentials-source.database")
public record DatabaseCredentialsSourceProperties(
    boolean enabled,
    DatabaseDriver dbDriver,
    String dbHost,
    int dbPort,
    String dbName,
    Duration maxIdleTime,
    int initialPoolSize,
    int maxPoolSize,
    String lockTimeout,
    String statementTimeout) implements DatabaseProperties {}
