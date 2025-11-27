package javasabr.mqtt.model.acl.rule;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.AllOfCondition;

public record AllowPublishRule(AllOfCondition clientsAndTopics) implements Rule {
  @Override
  public Operation operation() {
    return Operation.PUBLISH;
  }

  @Override
  public Action action() {
    return Action.ALLOW;
  }
}
