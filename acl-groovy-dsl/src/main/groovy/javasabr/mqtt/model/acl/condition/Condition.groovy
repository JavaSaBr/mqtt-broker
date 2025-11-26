package javasabr.mqtt.model.acl.condition

import javasabr.mqtt.model.acl.CallId

interface Condition {

  boolean test(CallId callId)

  default Condition not(Condition condition) {
    return { !condition.test(it) }
  }
}
