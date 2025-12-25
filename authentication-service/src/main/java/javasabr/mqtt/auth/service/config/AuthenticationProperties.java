package javasabr.mqtt.auth.service.config;

import java.util.List;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "authentication")
public record AuthenticationProperties(
    boolean allowAnonymous,
    @Nullable String defaultProvider,
    @Nullable List<String> providers,
    @Nullable List<String> credentialsSources) {

  public AuthenticationProperties {
    if (!allowAnonymous && (providers == null || providers.isEmpty())) {
      throw new AuthenticationConfigException("Authenticator providers are not configured");
    }
  }
}
