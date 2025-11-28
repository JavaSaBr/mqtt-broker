//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.acl.rule.AllowSubscribeRule
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class AllowSubscribeRuleBuilder extends SubscribeRuleBuilder {
  private MutableArray<ValueMatcher<String>> topicFilters = ArrayFactory.mutableArray(ValueMatcher)

  AllowSubscribeRuleBuilder() { super(Action.ALLOW) }

  Rule build() {
    new AllowSubscribeRule(new AllOfCondition(clients, new TopicCondition(topicFilters)))
  }
}
