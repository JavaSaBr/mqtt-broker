package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.matcher.AnyTopicMatcher;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.rlib.collections.array.Array;

public record TopicCondition(Array<ValueMatcher<AbstractTopic>> topics) implements Condition<AbstractTopic> {

  public static final TopicCondition MATCH_ANY = new TopicCondition(Array.of(new AnyTopicMatcher()));

  @Override
  public boolean test(AbstractTopic value) {
    for (var topic : topics()) {
      if (topic.test(value)) {
        return true;
      }
    }
    return false;
  }
}
