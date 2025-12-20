package javasabr.mqtt.broker.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "credentials.source.db.reader")
public record DatabaseCredentialReaderProperties(String username, String password) {}
