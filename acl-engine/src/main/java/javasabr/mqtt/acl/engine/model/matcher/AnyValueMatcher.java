package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;

public record AnyValueMatcher() implements ValueMatcher<String> {
 
  private static final AnyValueMatcher INSTANCE = new AnyValueMatcher();

  public static AnyValueMatcher instance() {
    return INSTANCE;
  }

  @Override
  public boolean test(String value) {
    return true;
  }

  @Override
  public String toString() {
    return "AnyValue";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
