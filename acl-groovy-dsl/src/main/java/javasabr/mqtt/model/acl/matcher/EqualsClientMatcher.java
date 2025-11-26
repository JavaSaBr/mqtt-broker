package javasabr.mqtt.model.acl.matcher;

import java.util.Objects;

public record EqualsClientMatcher(String expectedValue) implements ClientMatcher<String> {

  @Override
  public boolean test(String value) {
    return Objects.equals(expectedValue, value);
  }
}
