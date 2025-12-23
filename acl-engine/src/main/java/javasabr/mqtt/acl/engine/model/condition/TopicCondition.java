package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.acl.engine.model.matcher.AnyTopicMatcher;
import javasabr.mqtt.acl.engine.model.matcher.TopicMatcher;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.rlib.collections.array.Array;

public record TopicCondition(Array<TopicMatcher> matchers) {

  public static final TopicCondition MATCH_ANY = new TopicCondition(Array.of(new AnyTopicMatcher()));

  public boolean test(MqttUser user, AbstractTopic topic) {
    for (TopicMatcher matcher : matchers) {
      if (matcher.test(user, topic)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(matchers);
  }
  
  @JsonValue
  Object jsonDebugValue() {
    return matchers;
  }
}
