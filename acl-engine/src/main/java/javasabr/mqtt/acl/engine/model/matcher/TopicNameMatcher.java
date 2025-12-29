package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicName;

public record TopicNameMatcher(TopicName expected) implements TopicMatcher {

  @Override
  public boolean test(MqttUser user, AbstractTopic topic) {
    return expected.isMatched(topic);
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
