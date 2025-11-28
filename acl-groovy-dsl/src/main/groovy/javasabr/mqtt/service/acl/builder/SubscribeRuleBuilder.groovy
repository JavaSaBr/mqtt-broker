//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class SubscribeRuleBuilder extends RuleBuilder {
  protected MutableArray<ValueMatcher<String>> topicFilters = ArrayFactory.mutableArray(ValueMatcher)

  SubscribeRuleBuilder(Action permission) { super(permission, Operation.SUBSCRIBE) }

  SubscribeRuleBuilder topicFilter(ValueMatcher<String>... topicFilter) {
    this.topicFilters.addAll(topicFilter); this
  }
}
