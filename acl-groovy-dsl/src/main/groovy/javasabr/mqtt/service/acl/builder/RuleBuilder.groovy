//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.mqtt.model.exception.AclConfigurationException

abstract class RuleBuilder implements TopicMatcherBuilder {
  Action action
  Operation operation
  MqttUserCondition userCondition

  RuleBuilder(Action action, Operation operation) {
    this.action = action
    this.operation = operation
  }

  RuleBuilder allOf(Closure<?> config) {
    if (this.userCondition) {
      throw new AclConfigurationException("Only one clients section allowed")
    }
    this.userCondition = new AllOfBuilder().buildCondition(config).build()
    return this
  }

  RuleBuilder anyOf(Closure<?> config) {
    if (this.userCondition) {
      throw new AclConfigurationException("Only one clients section allowed")
    }
    this.userCondition = config == null ? MqttUserCondition.MATCH_ANY : new AnyOfBuilder().buildCondition(config).build()
    return this
  }

  static ValueMatcher<?> anyone() {
    return ValueMatcher.MATCH_ANY
  }

  abstract Rule build()
}
