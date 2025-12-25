package javasabr.mqtt.auth.service.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javasabr.mqtt.auth.service.config.condition.AnonymousProviderCondition;
import org.springframework.context.annotation.Conditional;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AnonymousProviderCondition.class)
public @interface ConditionalOnAnonymousProvider {
}
