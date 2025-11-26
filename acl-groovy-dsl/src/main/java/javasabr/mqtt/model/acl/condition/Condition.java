package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;

public interface Condition {

  boolean test(CallId callId);

  default Condition not(Condition condition) {
    return callId -> !condition.test(callId);
  }
}
