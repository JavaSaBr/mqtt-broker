package javasabr.mqtt.auth.service.config.condition;

import java.util.Map;
import java.util.Objects;
import javasabr.mqtt.auth.service.config.AuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

public abstract class AuthenticationConfigCondition extends SpringBootCondition {

  public ConditionOutcome doFredAgain(Environment env, Map<String, Object> attributes, String resource) {
    String id = Objects.requireNonNullElse(attributes, Map.of())
        .getOrDefault("value", "none")
        .toString();
    return Binder.get(env)
        .bind("authentication", AuthenticationProperties.class)
        .map(sources -> isCredentialsSourceEnabled(sources, id))
        .map(isCredentialsSourceEnabled -> isCredentialsSourceEnabled
               ? ConditionOutcome.match("%s '%s' enabled".formatted(resource, id))
               : ConditionOutcome.noMatch("%s '%s' disabled".formatted(resource, id)))
        .orElse(ConditionOutcome.noMatch("Authentication properties not found"));
  }

  abstract boolean isCredentialsSourceEnabled(AuthenticationProperties properties, String value);
}