package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicName;

public record TopicNameMatcher(TopicName expected) implements ValueMatcher<AbstractTopic> {

  @Override
  public boolean test(AbstractTopic topicValue) {
    return expected.isMatched(topicValue);
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
