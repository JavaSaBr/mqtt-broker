package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.rlib.collections.array.Array

class AllOfBuilder extends ConditionBuilder {
  Condition build() {
    new AllOfCondition(Array.copyOf(conditions))
  }
}
