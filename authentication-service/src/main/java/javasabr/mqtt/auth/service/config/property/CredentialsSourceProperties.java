package javasabr.mqtt.auth.service.config.property;

import java.time.Duration;

public record CredentialsSourceProperties(
    boolean enabled,
    String fsPath,
    DatabaseDriver dbDriver,
    String dbHost,
    int dbPort,
    String dbName,
    Duration maxIdleTime,
    int initialPoolSize,
    int maxPoolSize,
    String lockTimeout,
    String statementTimeout
) implements FileCredentialsSourceProperties, DatabasePoolConfig, DatabaseUrlConfig, DatabaseTimeoutsConfig {}
