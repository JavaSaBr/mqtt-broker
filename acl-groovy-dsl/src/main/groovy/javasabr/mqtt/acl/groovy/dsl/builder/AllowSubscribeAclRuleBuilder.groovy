//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.rule.AclRule
import javasabr.mqtt.acl.engine.model.rule.AllowSubscribeAclRule

class AllowSubscribeAclRuleBuilder extends SubscribeAclRuleBuilder {

  AllowSubscribeAclRuleBuilder() { super(Action.ALLOW) }

  @Override
  AclRule buildImpl() {
    return new AllowSubscribeAclRule(userCondition, new TopicCondition(topicMatchers))
  }
}
