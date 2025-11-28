//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.rule.AllowPublishRule
import javasabr.mqtt.model.acl.rule.Rule

class AllowPublishRuleBuilder extends PublishRuleBuilder {

  AllowPublishRuleBuilder() { super(Action.ALLOW) }

  Rule build() {
    new AllowPublishRule(new AllOfCondition(clients, new TopicCondition(topicNames)))
  }
}
