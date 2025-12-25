package javasabr.mqtt.auth.service.config.condition;

import javasabr.mqtt.auth.service.config.AuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AnonymousProviderCondition extends SpringBootCondition {
  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return Binder.get(context.getEnvironment())
        .bind("authentication", AuthenticationProperties.class)
        .map(authProps -> {
          if (authProps.allowAnonymous()) {
            return ConditionOutcome.match("Anonymous connections allowed");
          } else {
            return ConditionOutcome.noMatch("Anonymous connections denied");
          }
        })
        .orElse(ConditionOutcome.noMatch("Authentication providers are not configured"));
  }
}