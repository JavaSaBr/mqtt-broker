//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.rlib.collections.array.Array

class AnyOfUserConditionBuilder extends UserConditionBuilder {

  AnyOfUserConditionBuilder allOf(Closure<?> config) {
    this.conditions.add(new AllOfUserConditionBuilder().configure(config).build())
    return this
  }

  AnyOfUserConditionBuilder anyOf(Closure<?> config) {
    this.conditions.add(new AnyOfUserConditionBuilder().configure(config).build())
    return this
  }

  MqttUserCondition build() {
    return new AnyOfCondition(Array.copyOf(conditions))
  }
}
