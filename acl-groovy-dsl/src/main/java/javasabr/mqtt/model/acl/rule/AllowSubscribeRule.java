package javasabr.mqtt.model.acl.rule;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.Condition;
import javasabr.mqtt.model.acl.matcher.ValueMatcher;
import javasabr.rlib.collections.array.Array;

public record AllowSubscribeRule(Condition clients, Condition topics) implements Rule{
  @Override
  public Operation operation() {
    return Operation.SUBSCRIBE;
  }

  @Override
  public Action action() {
    return Action.ALLOW;
  }
}
