package javasabr.mqtt.auth.service.config.property;

import javasabr.mqtt.auth.api.DatabaseTimeoutsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authentication.credentials-source.database.timeout")
public record SpringDatabaseTimeoutsProperties(String lockTimeout, String statementTimeout) implements
    DatabaseTimeoutsProperties {}

