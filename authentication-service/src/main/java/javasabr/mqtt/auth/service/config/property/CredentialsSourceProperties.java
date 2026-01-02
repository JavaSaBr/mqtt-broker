package javasabr.mqtt.auth.service.config.property;

import java.net.URI;
import java.time.Duration;

public record CredentialsSourceProperties(
    boolean enabled,
    URI fsPath,
    DatabaseDriver dbDriver,
    String dbHost,
    int dbPort,
    String dbName,
    Duration maxIdleTime,
    int initialPoolSize,
    int maxPoolSize,
    String lockTimeout,
    String statementTimeout
) implements FileConfig, DatabasePoolConfig, DatabaseUrlConfig, DatabaseTimeoutsConfig {}
