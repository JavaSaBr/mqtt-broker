package javasabr.mqtt.model.acl.matcher;

import java.util.Objects;

public record TopicNameMatcher(String expectedValue) implements TopicMatcher<String> {

  @Override
  public boolean test(String value) {
    return Objects.equals(expectedValue, value);
  }
}
