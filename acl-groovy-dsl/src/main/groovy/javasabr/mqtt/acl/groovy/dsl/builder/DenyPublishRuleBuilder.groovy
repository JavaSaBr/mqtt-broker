//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.rule.DenyPublishRule
import javasabr.mqtt.acl.engine.model.rule.Rule

class DenyPublishRuleBuilder extends PublishRuleBuilder {

  DenyPublishRuleBuilder() { super(Action.DENY) }

  Rule build() {
    return new DenyPublishRule(userCondition, new TopicCondition(topicMatchers))
  }
}
