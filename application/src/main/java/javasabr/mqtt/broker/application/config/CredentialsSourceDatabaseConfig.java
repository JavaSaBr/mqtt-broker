package javasabr.mqtt.broker.application.config;

import java.time.Duration;
import javasabr.mqtt.service.auth.source.CredentialsSourceConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "credentials.source.db")
public record CredentialsSourceDatabaseConfig(
    String username,
    String password,
    String driver,
    String host,
    int port,
    String name,
    String credentialsQuery,
    Duration maxIdleTime,
    int initialPoolSize,
    int maxPoolSize,
    String lockTimeout,
    String statementTimeout) implements CredentialsSourceConfig {}
