package javasabr.mqtt.auth.service.config.condition;

import java.util.List;
import java.util.Objects;
import javasabr.mqtt.auth.service.config.property.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnAuthenticationProvider;

public class AuthenticationProviderCondition extends AuthenticationConfigCondition {
  @Override
  boolean isEnabled(AuthenticationProperties properties, String value) {
    return Objects.requireNonNullElse(properties.providers(), List.of()).contains(value);
  }

  Class<?> annotation() {
    return ConditionalOnAuthenticationProvider.class;
  }
}