package javasabr.mqtt.model.acl;

import javasabr.mqtt.model.acl.condition.AnyCondition;
import javasabr.mqtt.model.acl.condition.Condition;
import javasabr.mqtt.model.acl.matcher.AnyTopic;
import javasabr.mqtt.model.acl.matcher.TopicMatcher;
import javasabr.rlib.collections.array.Array;

public record Rule(Action action, Operation operation, Condition condition, Array<TopicMatcher<String>> topics) {

  private static final Array<TopicMatcher<String>> ANY_TOPIC = Array.of(new AnyTopic());
  public static final Condition ANY_CONDITION = new AnyCondition();

  public Rule(Action action, Operation operation) {
    this(action, operation, ANY_CONDITION, ANY_TOPIC);
  }
}
