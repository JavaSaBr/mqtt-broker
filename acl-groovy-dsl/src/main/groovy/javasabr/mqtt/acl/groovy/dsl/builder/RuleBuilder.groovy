//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import groovy.transform.TypeChecked
import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.mqtt.acl.engine.model.rule.Rule
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.topic.AbstractTopic
import javasabr.rlib.collections.array.Array

abstract class RuleBuilder {
  Action action
  Operation operation
  
  MqttUserCondition userCondition
  Array<ValueMatcher<AbstractTopic>> topicMatchers

  RuleBuilder(Action action, Operation operation) {
    this.action = action
    this.operation = operation
  }

  @TypeChecked
  RuleBuilder users(Closure<?> config) {
    if (userCondition != null) {
      throw new AclConfigurationException("Only one users section allowed")
    }
    userCondition = new UsersBuilder().configure(config).build()
    return this
  }

  @TypeChecked
  RuleBuilder topics(Closure<?> config) {
    if (topicMatchers != null) {
      throw new AclConfigurationException("Only one topics section allowed")
    }
    topicMatchers = new TopicsBuilder().configure(config).build()
    return this
  }
  
  abstract Rule build()
}
