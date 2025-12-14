//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.rule.AllowSubscribeRule
import javasabr.mqtt.acl.engine.model.rule.Rule

class AllowSubscribeRuleBuilder extends SubscribeRuleBuilder {

  AllowSubscribeRuleBuilder() { super(Action.ALLOW) }

  Rule build() {
    return new AllowSubscribeRule(userCondition, new TopicCondition(topicFilters))
  }
}
