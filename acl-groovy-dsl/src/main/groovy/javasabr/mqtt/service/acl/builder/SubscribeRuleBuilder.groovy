//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class SubscribeRuleBuilder extends RuleBuilder {
  protected MutableArray<ValueMatcher<TopicFilter>> topicFilters = ArrayFactory.mutableArray(ValueMatcher)

  SubscribeRuleBuilder(Action action) { super(action, Operation.SUBSCRIBE) }

  SubscribeRuleBuilder topicFilter(ValueMatcher<TopicFilter>... topicFilters) {
    this.topicFilters.addAll(topicFilters)
    return this
  }
}
