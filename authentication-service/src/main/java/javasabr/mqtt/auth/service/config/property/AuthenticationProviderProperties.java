package javasabr.mqtt.auth.service.config.property;

import java.util.Map;
import javasabr.mqtt.auth.api.CredentialsSourceType;

public record AuthenticationProviderProperties(
    boolean enabled,
    Map<CredentialsSourceType, CredentialsSourceProperties> credentialsSources) implements BasicAuthenticationProviderProperties{}
