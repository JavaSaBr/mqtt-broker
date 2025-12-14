package javasabr.mqtt.acl.engine.model.matcher;

import java.util.Objects;

public record EqualsMatcher(String expectedValue) implements ValueMatcher<String> {

  @Override
  public boolean test(String value) {
    return Objects.equals(expectedValue, value);
  }
}
