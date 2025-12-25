package javasabr.mqtt.auth.service.config.condition;

import java.util.Map;
import java.util.Objects;
import javasabr.mqtt.auth.service.config.AuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public abstract class AuthenticationConfigCondition extends SpringBootCondition {

  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Map<String, Object> attributes = metadata.getAnnotationAttributes(annotation().getName());
    attributes = Objects.requireNonNullElse(attributes, Map.of());
    String resourceId = attributes
        .getOrDefault("value", "none")
        .toString();
    String resource = attributes
        .getOrDefault("resource", "unknown")
        .toString();
    return Binder.get(context.getEnvironment())
        .bind("authentication", AuthenticationProperties.class)
        .map(properties -> isEnabled(properties, resourceId))
        .map(isCredentialsSourceEnabled -> isCredentialsSourceEnabled
                                           ? ConditionOutcome.match("%s '%s' enabled".formatted(resource, resourceId))
                                           : ConditionOutcome.noMatch("%s '%s' disabled".formatted(resource, resourceId)))
        .orElse(ConditionOutcome.noMatch("Authentication properties not defined"));

  }

  abstract boolean isEnabled(AuthenticationProperties properties, String value);

  abstract Class<?> annotation();
}