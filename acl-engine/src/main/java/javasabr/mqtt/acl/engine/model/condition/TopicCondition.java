package javasabr.mqtt.acl.engine.model.condition;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.acl.engine.model.matcher.AnyTopicMatcher;
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.rlib.collections.array.Array;
import org.jspecify.annotations.Nullable;

public record TopicCondition(Array<ValueMatcher<AbstractTopic>> matchers) implements Condition<AbstractTopic> {

  public static final TopicCondition MATCH_ANY = new TopicCondition(Array.of(new AnyTopicMatcher()));

  @Override
  public boolean test(@Nullable AbstractTopic requestedTopic) {
    if (requestedTopic == null) {
      return false;
    }
    for (ValueMatcher<AbstractTopic> topic : matchers) {
      if (topic.test(requestedTopic)) {
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
