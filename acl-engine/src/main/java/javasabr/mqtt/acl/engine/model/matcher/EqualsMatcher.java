package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public record EqualsMatcher<T>(T expected) implements ValueMatcher<T> {

  @Override
  public boolean test(T value) {
    return Objects.equals(expected, value);
  }

  @Override
  public String toString() {
    return "Eq:[" + expected + "]";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
