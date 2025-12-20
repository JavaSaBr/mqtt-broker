package javasabr.mqtt.broker.application.config;

import java.time.Duration;
import java.util.Map;
import javasabr.mqtt.model.Credentials;
import javasabr.mqtt.model.database.DatabasePoolConfig;
import javasabr.mqtt.model.database.DatabaseTimeoutsConfig;
import javasabr.mqtt.model.database.DatabaseUrlConfig;
import javasabr.mqtt.model.database.DatabaseUsersConfig;
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
