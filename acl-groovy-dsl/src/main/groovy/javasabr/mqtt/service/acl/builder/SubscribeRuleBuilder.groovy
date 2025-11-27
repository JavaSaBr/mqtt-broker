//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class SubscribeRuleBuilder extends RuleBuilder {
  private MutableArray<ValueMatcher<String>> topicFilters = ArrayFactory.mutableArray(ValueMatcher)

  private SubscribeRuleBuilder(Action permission) { super(permission, Operation.SUBSCRIBE) }

  private SubscribeRuleBuilder topicFilter(ValueMatcher<String>... topicFilter) {
    this.topicFilters.addAll(topicFilter); this
  }

  Rule build() { new Rule(permission, action, condition, topicFilters) }
}
