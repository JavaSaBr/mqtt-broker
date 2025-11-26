package javasabr.mqtt.model.acl.condition

import javasabr.mqtt.model.acl.CallId
import javasabr.rlib.collections.array.Array

record AllOfCondition(Array<Condition> conditions) implements Condition {

  @Override
  boolean test(CallId callId) {
    for (Condition condition : conditions) {
      if(!condition.test(callId)) {
        return false
      }
    }
    return true
  }
}
