package javasabr.mqtt.model.acl.condition;

import javasabr.mqtt.model.acl.CallId;

record AnyCondition() implements Condition {

  @Override
  boolean test(CallId callId) {
    return true
  }
}
