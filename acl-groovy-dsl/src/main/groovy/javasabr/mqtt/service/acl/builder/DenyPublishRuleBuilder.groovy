//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.acl.rule.DenyPublishRule
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class DenyPublishRuleBuilder extends PublishRuleBuilder {
  private MutableArray<ValueMatcher<String>> topicNames = ArrayFactory.mutableArray(ValueMatcher)

  DenyPublishRuleBuilder() { super(Action.DENY) }

  Rule build() {
    new DenyPublishRule(new AllOfCondition(clients, new TopicCondition(topicNames)))
  }
}
