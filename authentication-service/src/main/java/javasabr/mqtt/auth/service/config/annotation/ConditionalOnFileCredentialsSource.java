package javasabr.mqtt.auth.service.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnCredentialsSource("file")
@ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.FileCredentialsSource")
public @interface ConditionalOnFileCredentialsSource {
}
