package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

public record RegexMatcher(Pattern pattern) implements ValueMatcher<String> {

  @Override
  public boolean test(String value) {
    return pattern.matcher(value).matches();
  }

  @Override
  public String toString() {
    return "Regex:[" + pattern + "]";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
