//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.condition.AllOfCondition
import javasabr.mqtt.model.acl.condition.TopicCondition
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.mqtt.model.acl.rule.AllowPublishRule
import javasabr.mqtt.model.acl.rule.DenyPublishRule
import javasabr.mqtt.model.acl.rule.Rule
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

import static javasabr.mqtt.model.acl.Action.ALLOW

class PublishRuleBuilder extends RuleBuilder {
  private MutableArray<ValueMatcher<String>> topicNames = ArrayFactory.mutableArray(ValueMatcher)

  PublishRuleBuilder(Action permission) { super(permission, Operation.PUBLISH) }

  private PublishRuleBuilder topicName(ValueMatcher<String>... topicName) { this.topicNames.addAll(topicName); this }

  Rule build() {
    permission == ALLOW
        ? new AllowPublishRule(new AllOfCondition(clients, new TopicCondition(topicNames)))
        : new DenyPublishRule(new AllOfCondition(clients, new TopicCondition(topicNames)))
  }
}
