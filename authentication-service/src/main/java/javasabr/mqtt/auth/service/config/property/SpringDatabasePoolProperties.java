package javasabr.mqtt.auth.service.config.property;

import java.time.Duration;
import javasabr.mqtt.auth.api.DatabasePoolProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authentication.credentials-source.database.pool")
public record SpringDatabasePoolProperties(Duration maxIdleTime, int initialSize, int maxSize) implements
    DatabasePoolProperties {}

