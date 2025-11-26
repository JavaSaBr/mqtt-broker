package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.condition.Condition

abstract class RuleBuilder implements TopicMatcherBuilder {
  Permission permission
  Action action
  Condition condition

  RuleBuilder(Permission permission, Action action) { this.permission = permission; this.action = action }

  RuleBuilder allOf(Closure<?> config) {
    if (this.condition) throw new IllegalArgumentException("Only one clients section allowed")
    this.condition = new AllOfBuilder().buildCondition(config).build()
    return this
  }

  RuleBuilder anyOf(Closure<?> config) {
    if (this.condition) throw new IllegalArgumentException("Only one clients section allowed")
    this.condition = new AnyOfBuilder().buildCondition(config).build()
    return this
  }

  RuleBuilder any() { this.condition = MATCH_ANY; this }

  abstract Rule build()
}
