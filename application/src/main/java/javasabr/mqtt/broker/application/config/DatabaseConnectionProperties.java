package javasabr.mqtt.broker.application.config;

import java.time.Duration;
import java.util.Map;
import javasabr.mqtt.auth.credentials.source.config.Credentials;
import javasabr.mqtt.auth.credentials.source.config.DatabasePoolConfig;
import javasabr.mqtt.auth.credentials.source.config.DatabaseTimeoutsConfig;
import javasabr.mqtt.auth.credentials.source.config.DatabaseUrlConfig;
import javasabr.mqtt.auth.credentials.source.config.DatabaseUsersConfig;
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
