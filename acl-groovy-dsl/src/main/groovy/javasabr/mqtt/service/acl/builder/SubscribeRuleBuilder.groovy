//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.acl.rule.AllowSubscribeRule
import javasabr.mqtt.model.acl.rule.DenySubscribeRule
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

import static javasabr.mqtt.model.acl.Action.ALLOW

class SubscribeRuleBuilder extends RuleBuilder {
  private MutableArray<ValueMatcher<String>> topicFilters = ArrayFactory.mutableArray(ValueMatcher)

  SubscribeRuleBuilder(Action permission) { super(permission, Operation.SUBSCRIBE) }

  private SubscribeRuleBuilder topicFilter(ValueMatcher<String>... topicFilter) {
    this.topicFilters.addAll(topicFilter); this
  }

  Rule build() {
    permission == ALLOW
        ? new AllowSubscribeRule(condition, new TopicCondition(topicFilters))
        : new DenySubscribeRule(condition, new TopicCondition(topicFilters))
  }
}
