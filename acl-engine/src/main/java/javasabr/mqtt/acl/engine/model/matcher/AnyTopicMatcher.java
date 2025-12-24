package javasabr.mqtt.acl.engine.model.matcher;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.topic.AbstractTopic;

public record AnyTopicMatcher() implements ValueMatcher<AbstractTopic> {

  private static final AnyTopicMatcher INSTANCE = new AnyTopicMatcher();

  public static AnyTopicMatcher instance() {
    return INSTANCE;
  }

  @Override
  public boolean test(AbstractTopic requestedTopic) {
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
