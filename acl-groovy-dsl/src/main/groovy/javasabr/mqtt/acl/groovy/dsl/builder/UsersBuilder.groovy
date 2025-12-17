package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.condition.AnyOfCondition
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.rlib.collections.array.Array

class UsersBuilder extends UserConditionBuilder {
  
  UsersBuilder allOf(Closure<?> config) {
    conditions.add(new AllOfUserConditionBuilder().configure(config).build())
    return this
  }

  UsersBuilder anyOf(Closure<?> config) {
    conditions.add(new AnyOfUserConditionBuilder().configure(config).build())
    return this
  }
  
  @Override
  MqttUserCondition build() {
    if (conditions.size() > 1) {
      return new AnyOfCondition(Array.copyOf(conditions))
    }
    return conditions.isEmpty() ? MqttUserCondition.MATCH_NONE : conditions.first()
  }
}
