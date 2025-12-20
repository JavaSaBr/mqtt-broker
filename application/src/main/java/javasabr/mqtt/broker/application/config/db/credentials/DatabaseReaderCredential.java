package javasabr.mqtt.broker.application.config.db.credentials;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "credentials.source.db.reader")
public record DatabaseReaderCredential(String username, String password) {}
