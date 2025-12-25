package javasabr.mqtt.auth.service.config.condition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import javasabr.mqtt.auth.service.config.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnAuthenticationProvider;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AuthenticationProviderCondition extends AuthenticationConfigCondition {
  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnAuthenticationProvider.class.getName());
    return doFredAgain(context.getEnvironment(), attrs, "Authentication Provider");
  }

  @Override
  boolean isCredentialsSourceEnabled(AuthenticationProperties properties, String value) {
    return Objects.requireNonNullElse(properties.providers(), List.of()).contains(value);
  }
}