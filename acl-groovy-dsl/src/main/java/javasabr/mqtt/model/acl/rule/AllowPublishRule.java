package javasabr.mqtt.model.acl.rule;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.Condition;

public record AllowPublishRule(Condition clients, Condition topics) implements Rule {
  @Override
  public Operation operation() {
    return Operation.PUBLISH;
  }

  @Override
  public Action action() {
    return Action.ALLOW;
  }
}
