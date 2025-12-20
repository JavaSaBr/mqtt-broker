package javasabr.mqtt.broker.application.config.db.credentials;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "credentials.source.db.admin")
public record DatabaseAdminCredential(String username, String password) {}
