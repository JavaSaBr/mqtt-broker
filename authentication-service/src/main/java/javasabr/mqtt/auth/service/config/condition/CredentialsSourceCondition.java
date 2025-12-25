package javasabr.mqtt.auth.service.config.condition;

import java.util.List;
import java.util.Map;
import javasabr.mqtt.auth.service.config.AuthenticationProperties;
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnCredentialsSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CredentialsSourceCondition extends SpringBootCondition {
  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnCredentialsSource.class.getName());
    String requiredSource = attributes.getOrDefault("value", "").toString();

    return Binder.get(context.getEnvironment())
        .bind("authentication", AuthenticationProperties.class)
        .map(authProps -> {
          List<String> sources = authProps.credentialsSources();
          if (sources != null && sources.contains(requiredSource)) {
            return ConditionOutcome.match("Source '" + requiredSource + "' is enabled");
          }
          return ConditionOutcome.noMatch("Source '" + requiredSource + "' not in config");
        })
        .orElse(ConditionOutcome.noMatch("Authentication properties not found"));
  }
}