package javasabr.mqtt.model.acl;

import javasabr.mqtt.model.acl.condition.AnyCondition;
import javasabr.mqtt.model.acl.condition.Condition;
import javasabr.mqtt.model.acl.matcher.TopicMatcher;
import javasabr.rlib.collections.array.Array;

public record Rule(Action action, Operation operation, Condition condition, Array<TopicMatcher<String>> topics) {
  public Rule(Action action, Operation operation) {
    this(action, operation, new AnyCondition(), Array.empty(TopicMatcher.class));
  }
}
