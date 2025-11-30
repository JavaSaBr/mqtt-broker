package javasabr.mqtt.model.acl.matcher;

import java.util.Objects;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicName;

public record TopicNameMatcher(AbstractTopic expectedValue) implements ValueMatcher<AbstractTopic> {

  @Override
  public boolean test(AbstractTopic value) {
    return Objects.equals(expectedValue.rawTopic(), value.rawTopic());
  }
}
