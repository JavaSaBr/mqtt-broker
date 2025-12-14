//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.builder.TopicMatcherBuilder
import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.mqtt.acl.engine.model.rule.Rule
import javasabr.mqtt.model.acl.Operation

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
