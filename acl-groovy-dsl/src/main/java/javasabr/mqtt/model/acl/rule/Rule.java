package javasabr.mqtt.model.acl.rule;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.AllOfCondition;
import javasabr.mqtt.model.acl.condition.AnyCondition;
import javasabr.mqtt.model.acl.condition.Condition;

public interface Rule {

  Condition MATCH_ANY = new AnyCondition();

  Operation operation();

  Action action();

  AllOfCondition clientsAndTopics();

  default boolean test(CallId callId) {
    if (operation() != callId.operation()) {
      return false;
    }
    if (!clientsAndTopics().test(callId)) {
      return false;
    }
    return action() == Action.ALLOW;
  }
}
