package javasabr.mqtt.model.acl.condition

import javasabr.mqtt.model.acl.CallId
import javasabr.rlib.collections.array.Array

record AnyOfCondition(Array<Condition> conditions) implements Condition {

  @Override
  boolean test(CallId callId) {
    for (Condition condition : conditions) {
      if (condition.test(callId)) {
        return true
      }
    }
    return false
  }
}
