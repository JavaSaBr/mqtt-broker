//file:noinspection unused
package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.matcher.ValueMatcher
import javasabr.mqtt.model.acl.Operation
import javasabr.mqtt.model.topic.TopicName
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

abstract class PublishRuleBuilder extends RuleBuilder {
  protected MutableArray<ValueMatcher<TopicName>> topicNames = ArrayFactory.mutableArray(ValueMatcher)

  PublishRuleBuilder(Action action) {
    super(action, Operation.PUBLISH)
  }

  PublishRuleBuilder topicName(ValueMatcher<TopicName>... topicNames) {
    this.topicNames.addAll(topicNames)
    return this
  }
}
