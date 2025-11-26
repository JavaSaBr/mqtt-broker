package javasabr.mqtt.service.acl.builder

import javasabr.mqtt.model.acl.Action
import javasabr.mqtt.model.acl.Permission
import javasabr.mqtt.model.acl.Rule
import javasabr.mqtt.model.acl.value.matcher.TopicMatcher
import javasabr.mqtt.model.acl.value.matcher.TopicNameValueMatcher
import javasabr.rlib.collections.array.ArrayFactory
import javasabr.rlib.collections.array.MutableArray

class PublishRuleBuilder extends RuleBuilder {
  private MutableArray<TopicMatcher<String>> topicNames = ArrayFactory.mutableArray(TopicMatcher)

  private PublishRuleBuilder(Permission permission) { super(permission, Action.PUBLISH) }

  private PublishRuleBuilder topicName(TopicNameValueMatcher... topicName) { this.topicNames.addAll(topicName); this }

  Rule build() { new Rule(permission, action, condition/* ?: MATCH_ANY*/, topicNames) }
}
