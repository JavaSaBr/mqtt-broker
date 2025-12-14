//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.mqtt.model.acl.Operation
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
