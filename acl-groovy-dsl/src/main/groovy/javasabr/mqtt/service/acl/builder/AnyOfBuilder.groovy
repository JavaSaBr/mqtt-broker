package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.condition.AnyOfCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.rlib.collections.array.Array

class AnyOfBuilder extends ConditionBuilder {
  Condition build() { new AnyOfCondition(Array.copyOf(conditions)) }
}
