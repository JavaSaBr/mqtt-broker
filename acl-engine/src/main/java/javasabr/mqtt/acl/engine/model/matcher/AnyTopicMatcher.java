package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.AbstractTopic;

public record AnyTopicMatcher() implements TopicMatcher {

  private static final AnyTopicMatcher INSTANCE = new AnyTopicMatcher();

  public static AnyTopicMatcher instance() {
    return INSTANCE;
  }

  @Override
  public boolean test(MqttUser user, AbstractTopic topic) {
    return true;
  }
  
  @Override
  public String toString() {
    return "AnyTopic";
  }

  @JsonValue
  Object jsonDebugValue() {
    return toString();
  }
}
