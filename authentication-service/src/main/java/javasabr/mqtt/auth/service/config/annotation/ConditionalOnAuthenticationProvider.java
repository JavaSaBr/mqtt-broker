package javasabr.mqtt.auth.service.config.annotation;

import javasabr.mqtt.auth.service.config.condition.AuthenticationProviderCondition;
import org.springframework.context.annotation.Conditional;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AuthenticationProviderCondition.class)
public @interface ConditionalOnAuthenticationProvider {
    String value();
}