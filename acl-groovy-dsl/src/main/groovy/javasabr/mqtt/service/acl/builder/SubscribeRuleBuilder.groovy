package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.value.matcher.TopicMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class SubscribeRuleBuilder extends RuleBuilder {
  private MutableArray<TopicMatcher<String>> topicFilters = ArrayFactory.mutableArray(TopicMatcher)

  private SubscribeRuleBuilder(Action permission) { super(permission, Operation.SUBSCRIBE) }

  private SubscribeRuleBuilder topicFilter(TopicMatcher<String>... topicFilter) {
    this.topicFilters.addAll(topicFilter); this
  }

  Rule build() { new Rule(permission, action, condition/* ?: MATCH_ANY*/, topicFilters) }
}
