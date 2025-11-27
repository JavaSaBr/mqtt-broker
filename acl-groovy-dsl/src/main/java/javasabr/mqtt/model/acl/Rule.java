package javasabr.mqtt.model.acl;

import javasabr.mqtt.model.acl.condition.AnyCondition;
import javasabr.mqtt.model.acl.condition.Condition;
import javasabr.mqtt.model.acl.matcher.AnyValueMatcher;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import javasabr.rlib.collections.array.Array;

public record Rule(Action action, Operation operation, Condition condition, Array<ValueMatcher<String>> topics) {

  private static final Array<ValueMatcher<String>> ANY_TOPIC = Array.of(new AnyValueMatcher());
  public static final Condition ANY_CONDITION = new AnyCondition();

  public Rule(Action action, Operation operation) {
    this(action, operation, ANY_CONDITION, ANY_TOPIC);
  }
}
