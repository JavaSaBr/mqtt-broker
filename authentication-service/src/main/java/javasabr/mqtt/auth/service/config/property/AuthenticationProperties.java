package javasabr.mqtt.auth.service.config.property;

import java.util.Map;
import javasabr.mqtt.auth.api.AuthenticationMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authentication")
public record AuthenticationProperties(Map<AuthenticationMethod, DefaultProviderProperties> provider) {}

