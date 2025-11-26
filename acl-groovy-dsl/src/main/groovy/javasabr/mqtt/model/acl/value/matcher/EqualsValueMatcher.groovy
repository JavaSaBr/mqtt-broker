package javasabr.mqtt.model.acl.value.matcher;

import java.util.Objects;

public record EqualsValueMatcher(String expectedValue) implements ClientMatcher<String> {

  @Override
  public boolean test(String value) {
    return Objects.equals(expectedValue, value);
  }
}
