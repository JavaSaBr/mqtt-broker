package javasabr.mqtt.model.acl.value.matcher;

import java.util.Objects;

public record TopicFilterValueMatcher(String expectedValue) implements TopicMatcher<String> {

  @Override
  public boolean test(String value) {
    return Objects.equals(expectedValue, value);
  }
}
