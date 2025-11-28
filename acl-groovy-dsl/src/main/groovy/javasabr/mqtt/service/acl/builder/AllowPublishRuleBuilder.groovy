//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.acl.rule.AllowPublishRule
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class AllowPublishRuleBuilder extends PublishRuleBuilder {
  private MutableArray<ValueMatcher<String>> topicNames = ArrayFactory.mutableArray(ValueMatcher)

  AllowPublishRuleBuilder() { super(Action.ALLOW) }

  Rule build() {
    new AllowPublishRule(new AllOfCondition(clients, new TopicCondition(topicNames)))
  }
}
