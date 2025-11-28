package javasabr.mqtt.model.acl.rule;

import static javasabr.mqtt.model.acl.Action.DENY;
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.AllOfCondition;

public record DenySubscribeRule(AllOfCondition clientsAndTopics) implements Rule {
  @Override
  public Operation operation() {
    return SUBSCRIBE;
  }

  @Override
  public Action action() {
    return DENY;
  }
}
