package javasabr.mqtt.auth.service.config.property;

import java.util.Map;
import javasabr.mqtt.auth.api.AuthenticationType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authentication")
public record AuthenticationProperties(
    boolean allowAnonymous,
    AuthenticationType defaultProvider,
    Map<AuthenticationType, AuthenticationProviderProperties> provider) {}
