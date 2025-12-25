package javasabr.mqtt.auth.service.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javasabr.mqtt.auth.service.config.condition.AuthenticationProviderCondition;
import org.springframework.context.annotation.Conditional;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AuthenticationProviderCondition.class)
public @interface ConditionalOnAuthenticationProvider {
  String value();

  String resource() default "Authentication Provider";
}
