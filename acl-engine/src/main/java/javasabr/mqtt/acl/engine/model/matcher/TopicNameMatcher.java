package javasabr.mqtt.acl.engine.model.matcher;

import java.util.Objects;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicName;

public record TopicNameMatcher(TopicName expectedTopic) implements ValueMatcher<AbstractTopic> {

  @Override
  public boolean test(AbstractTopic requestedTopic) {
    return Objects.equals(expectedTopic.rawTopic(), requestedTopic.rawTopic());
  }
}
