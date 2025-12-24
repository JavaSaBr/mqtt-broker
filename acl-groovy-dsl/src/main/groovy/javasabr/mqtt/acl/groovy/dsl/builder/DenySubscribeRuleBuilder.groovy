//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.rule.DenySubscribeRule
import javasabr.mqtt.acl.engine.model.rule.Rule

class DenySubscribeRuleBuilder extends SubscribeRuleBuilder {

  DenySubscribeRuleBuilder() { super(Action.DENY) }

  Rule build() {
    return new DenySubscribeRule(userCondition, new TopicCondition(topicMatchers))
  }
}
