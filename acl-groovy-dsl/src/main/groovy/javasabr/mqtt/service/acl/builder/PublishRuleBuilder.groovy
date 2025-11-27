//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class PublishRuleBuilder extends RuleBuilder {
  private MutableArray<ValueMatcher<String>> topicNames = ArrayFactory.mutableArray(ValueMatcher)

  private PublishRuleBuilder(Action permission) { super(permission, Operation.PUBLISH) }

  private PublishRuleBuilder topicName(ValueMatcher<String>... topicName) { this.topicNames.addAll(topicName); this }

  Rule build() { new Rule(permission, action, condition, topicNames) }
}
