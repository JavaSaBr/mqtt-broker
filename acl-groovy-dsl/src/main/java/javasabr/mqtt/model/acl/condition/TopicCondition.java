package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import javasabr.rlib.collections.array.Array;

public record TopicCondition(Array<ValueMatcher<String>> topics) implements Condition {

  public TopicCondition(ValueMatcher<String> topics) {
    this(Array.of(topics));
  }

  @Override
  public boolean test(CallId callId) {
    for (ValueMatcher<String> topic : topics()) {
      if (topic.test(callId.topic())) {
        return true;
      }
    }
    return false;
  }
}
