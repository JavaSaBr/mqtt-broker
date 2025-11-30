//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.rule.DenySubscribeRule
import javasabr.mqtt.model.acl.rule.Rule

class DenySubscribeRuleBuilder extends SubscribeRuleBuilder {

  DenySubscribeRuleBuilder() { super(Action.DENY) }

  Rule build() {
    return new DenySubscribeRule(clients, new TopicCondition(topicFilters))
  }
}
