package javasabr.mqtt.auth.service.config.property;

import java.net.URI;
import javasabr.mqtt.auth.api.FileProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("authentication.credentials-source.file")
public record FileCredentialsSourceProperties(
    boolean enabled, URI uriPath) implements FileProperties {}
