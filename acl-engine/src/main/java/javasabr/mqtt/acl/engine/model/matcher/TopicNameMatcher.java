package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicName;

public record TopicNameMatcher(TopicName expectedTopic) implements ValueMatcher<AbstractTopic> {

  @Override
  public boolean test(AbstractTopic topicValue) {
    return expectedTopic.isMatched(topicValue);
  }

  @Override
  public String toString() {
    return "Eq:[" + expectedTopic + "]";
  }
  
  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
