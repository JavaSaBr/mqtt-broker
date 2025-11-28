//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.condition.AnyOfCondition
import javasabr.mqtt.model.acl.condition.Condition
import javasabr.rlib.collections.array.Array

class AnyOfBuilder extends ConditionBuilder {

  ConditionBuilder allOf(Closure<?> config) {
    this.conditions << new AllOfBuilder().buildCondition(config).build()
    return this
  }

  ConditionBuilder anyOf(Closure<?> config) {
    this.conditions << new AnyOfBuilder().buildCondition(config).build()
    return this
  }

  Condition build() { new AnyOfCondition(Array.copyOf(conditions)) }
}
