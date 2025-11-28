package javasabr.mqtt.model.acl.rule;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.CallId;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.AllOfCondition;
import javasabr.mqtt.model.acl.condition.AnyCondition;
import javasabr.mqtt.model.acl.condition.Condition;

public sealed interface Rule permits AllowPublishRule, AllowSubscribeRule, DenyPublishRule, DenySubscribeRule {

  Condition MATCH_ANY = new AnyCondition();

  Operation operation();

  Action action();

  AllOfCondition clientsAndTopics();

  default boolean test(CallId callId) {
    return operation() == callId.operation() && clientsAndTopics().test(callId);
  }
}
