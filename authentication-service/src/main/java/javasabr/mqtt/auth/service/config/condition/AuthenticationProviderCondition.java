package javasabr.mqtt.auth.service.config.condition;

import java.util.List;
import java.util.Map;
import javasabr.mqtt.auth.service.config.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnAuthenticationProvider;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AuthenticationProviderCondition extends SpringBootCondition {
  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnAuthenticationProvider.class.getName());
    String requiredProvider = attributes.getOrDefault("value", "").toString();

    return Binder.get(context.getEnvironment())
        .bind("authentication", AuthenticationProperties.class)
        .map(authProps -> {
          List<String> providers = authProps.providers();
          if (providers != null && providers.contains(requiredProvider)) {
            return ConditionOutcome.match("Provider '" + requiredProvider + "' found in MqttProperties");
          } else {
            return ConditionOutcome.noMatch("Provider '" + requiredProvider + "' not active");
          }
        })
        .orElse(ConditionOutcome.noMatch("Authentication properties not found"));
  }
}