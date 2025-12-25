package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;

public record ContainsMatcher(String substring) implements ValueMatcher<String> {

  @Override
  public boolean test(String value) {
    return value.contains(substring);
  }

  @Override
  public String toString() {
    return "Contains:[" + substring + "]";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
