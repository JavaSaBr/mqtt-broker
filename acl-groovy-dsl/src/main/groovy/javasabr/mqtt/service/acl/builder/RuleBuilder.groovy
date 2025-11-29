//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.condition.MqttUserCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.mqtt.model.exception.AclConfigurationException

abstract class RuleBuilder implements ValueMatcherBuilder {
  Action permission
  Operation action
  MqttUserCondition clients

  RuleBuilder(Action permission, Operation action) { this.permission = permission; this.action = action }

  RuleBuilder allOf(Closure<?> config) {
    if (this.clients) throw new AclConfigurationException("Only one clients section allowed")
    this.clients = new AllOfBuilder().buildCondition(config).build()
    return this
  }

  RuleBuilder anyOf(Closure<?> config) {
    if (this.clients) throw new AclConfigurationException("Only one clients section allowed")
    this.clients = config == null ? MqttUserCondition.MATCH_ANY : new AnyOfBuilder().buildCondition(config).build()
    return this
  }

  static ValueMatcher<String> anyone() {
    ValueMatcher.ANY
  }

  abstract Rule build()
}
