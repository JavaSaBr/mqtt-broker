//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.rule.DenyPublishRule
import javasabr.mqtt.model.acl.rule.Rule

class DenyPublishRuleBuilder extends PublishRuleBuilder {

  DenyPublishRuleBuilder() { super(Action.DENY) }

  Rule build() {
    new DenyPublishRule(clients, new TopicCondition(topicNames))
  }
}
