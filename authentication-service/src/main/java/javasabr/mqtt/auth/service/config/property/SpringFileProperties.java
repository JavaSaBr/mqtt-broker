package javasabr.mqtt.auth.service.config.property;

import java.net.URI;
import javasabr.mqtt.auth.api.FileProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authentication.credentials-source.file")
public record SpringFileProperties(boolean enabled, URI path) implements FileProperties {}
