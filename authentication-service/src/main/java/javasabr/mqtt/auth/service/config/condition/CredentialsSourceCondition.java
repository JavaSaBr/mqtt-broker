package javasabr.mqtt.auth.service.config.condition;

import java.util.List;
import java.util.Objects;
import javasabr.mqtt.auth.service.config.property.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnCredentialsSource;

public class CredentialsSourceCondition extends AuthenticationConfigCondition {
  @Override
  boolean isEnabled(AuthenticationProperties properties, String value) {
    return Objects.requireNonNullElse(properties.credentialsSources(), List.of()).contains(value);
  }

  Class<?> annotation() {
    return ConditionalOnCredentialsSource.class;
  }
}