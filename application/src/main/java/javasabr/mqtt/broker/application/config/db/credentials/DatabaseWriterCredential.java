package javasabr.mqtt.broker.application.config.db.credentials;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "credentials.source.db.writer")
public record DatabaseWriterCredential(String username, String password) {}
