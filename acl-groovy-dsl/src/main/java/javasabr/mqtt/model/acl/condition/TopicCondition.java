package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.matcher.AnyTopicMatcher;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import javasabr.mqtt.model.topic.AbstractTopic;
import javasabr.rlib.collections.array.Array;

public record TopicCondition(Array<ValueMatcher<AbstractTopic>> expectedTopics) implements Condition<AbstractTopic> {

  public static final TopicCondition MATCH_ANY = new TopicCondition(Array.of(new AnyTopicMatcher()));

  @Override
  public boolean test(AbstractTopic requestedTopic) {
    for (var topic : expectedTopics()) {
      if (topic.test(requestedTopic)) {
        return true;
      }
    }
    return false;
  }
}
