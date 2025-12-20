package javasabr.mqtt.broker.application.config.db;

import java.time.Duration;
import javasabr.mqtt.model.DatabaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "credentials.source.db")
public record DatabaseConnectionProperties(
    String driver,
    String host,
    int port,
    String name,
    String credentialsQuery,
    Duration maxIdleTime,
    int initialPoolSize,
    int maxPoolSize,
    String lockTimeout,
    String statementTimeout) implements DatabaseProperties {}
