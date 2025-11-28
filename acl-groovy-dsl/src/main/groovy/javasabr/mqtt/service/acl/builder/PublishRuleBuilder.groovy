//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.acl.matcher.ValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class PublishRuleBuilder extends RuleBuilder {
  private MutableArray<ValueMatcher<String>> topicNames = ArrayFactory.mutableArray(ValueMatcher)

  PublishRuleBuilder(Action permission) { super(permission, Operation.PUBLISH) }

  PublishRuleBuilder topicName(ValueMatcher<String>... topicName) { this.topicNames.addAll(topicName); this }
}
