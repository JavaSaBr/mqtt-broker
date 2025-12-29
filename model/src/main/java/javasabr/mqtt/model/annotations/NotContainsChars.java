package javasabr.mqtt.model.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To mark that return string cannot contains some chars
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(value={FIELD, LOCAL_VARIABLE, METHOD})
public @interface NotContainsChars {

  char[] value() default {};
}
