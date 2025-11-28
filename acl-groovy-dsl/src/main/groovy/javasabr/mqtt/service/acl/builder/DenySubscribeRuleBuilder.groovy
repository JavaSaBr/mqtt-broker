//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.acl.rule.DenySubscribeRule
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class DenySubscribeRuleBuilder extends SubscribeRuleBuilder {
  private MutableArray<ValueMatcher<String>> topicFilters = ArrayFactory.mutableArray(ValueMatcher)

  DenySubscribeRuleBuilder() { super(Action.DENY) }

  Rule build() {
    new DenySubscribeRule(new AllOfCondition(clients, new TopicCondition(topicFilters)))
  }
}
