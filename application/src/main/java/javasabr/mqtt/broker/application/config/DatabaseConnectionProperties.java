package javasabr.mqtt.broker.application.config;

import java.time.Duration;
import java.util.Map;
import javasabr.mqtt.model.DatabaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "persistence.database")
public record DatabaseConnectionProperties(
    Map<String, javasabr.mqtt.model.Credentials> users,
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
