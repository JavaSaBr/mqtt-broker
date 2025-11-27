package javasabr.mqtt.model.acl.rule;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.AllOfCondition;

public record DenySubscribeRule(AllOfCondition clientsAndTopics) implements Rule {
  @Override
  public Operation operation() {
    return Operation.SUBSCRIBE;
  }

  @Override
  public Action action() {
    return Action.DENY;
  }
}
