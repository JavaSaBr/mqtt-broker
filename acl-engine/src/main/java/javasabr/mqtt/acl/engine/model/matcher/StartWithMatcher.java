package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;

public record StartWithMatcher(String prefix) implements ValueMatcher<String> {

  @Override
  public boolean test(String value) {
    return value.startsWith(prefix);
  }

  @Override
  public String toString() {
    return "StartWith:[" + prefix + "]";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
