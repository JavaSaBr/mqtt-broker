package javasabr.mqtt.auth.service.config.property;

import java.net.URI;
import java.time.Duration;

public record CredentialsSourceProperties(
    boolean enabled,
    URI uriPath,
    DatabaseDriver dbDriver,
    String dbHost,
    int dbPort,
    String dbName,
    Duration maxIdleTime,
    int initialPoolSize,
    int maxPoolSize,
    String lockTimeout,
    String statementTimeout) implements FileProperties, DatabaseProperties {}
