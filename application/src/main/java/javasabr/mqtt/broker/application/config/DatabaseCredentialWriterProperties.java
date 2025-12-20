package javasabr.mqtt.broker.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "credentials.source.db.writer")
public record DatabaseCredentialWriterProperties(String username, String password) {}
