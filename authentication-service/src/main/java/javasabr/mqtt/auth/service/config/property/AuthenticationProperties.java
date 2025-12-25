package javasabr.mqtt.auth.service.config.property;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authentication")
public record AuthenticationProperties(
    boolean allowAnonymous,
    @Nullable List<String> providers,
    @Nullable List<String> credentialsSources) {}
