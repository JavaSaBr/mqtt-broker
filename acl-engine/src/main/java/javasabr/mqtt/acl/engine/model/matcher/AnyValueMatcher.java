package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;

public record AnyValueMatcher<T>() implements ValueMatcher<T> {
 
  private static final ValueMatcher<String> STRING_MATCHER = new AnyValueMatcher<>();

  public static ValueMatcher<String> stringMatcher() {
    return STRING_MATCHER;
  }

  @Override
  public boolean test(Object value) {
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
