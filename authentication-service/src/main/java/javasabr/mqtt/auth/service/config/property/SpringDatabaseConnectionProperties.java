package javasabr.mqtt.auth.service.config.property;

import javasabr.mqtt.auth.api.database.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.database.DatabaseDriver;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authentication.credentials-source.database")
public record SpringDatabaseConnectionProperties(
    boolean enabled,
    DatabaseDriver driver,
    String host,
    int port,
    String name) implements DatabaseConnectionProperties {}

