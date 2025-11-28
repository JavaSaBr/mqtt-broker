package javasabr.mqtt.model.acl.rule;

import static javasabr.mqtt.model.acl.Action.ALLOW;
import static javasabr.mqtt.model.acl.Operation.PUBLISH;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.AllOfCondition;

public record AllowPublishRule(AllOfCondition clientsAndTopics) implements Rule {
  @Override
  public Operation operation() {
    return PUBLISH;
  }

  @Override
  public Action action() {
    return ALLOW;
  }
}
