//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.rule.AllowPublishRule
import javasabr.mqtt.acl.engine.model.rule.Rule

class AllowPublishRuleBuilder extends PublishRuleBuilder {

  AllowPublishRuleBuilder() { super(Action.ALLOW) }

  Rule build() {
    return new AllowPublishRule(userCondition, new TopicCondition(topicMatchers))
  }
}
