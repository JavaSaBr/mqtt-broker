package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.mqtt.model.topic.TopicFilter;

public record TopicFilterMatcher(TopicFilter expected) implements TopicMatcher<AbstractTopic> {
  
  @Override
  public boolean test(MqttUser user, AbstractTopic topic) {
    return expected.isMatched(topic);
  }
  
  @Override
  public String toString() {
    return "Match:[" + expected + "]";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
