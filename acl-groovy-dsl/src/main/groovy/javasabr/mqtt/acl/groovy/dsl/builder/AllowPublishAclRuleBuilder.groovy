//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.rule.AclRule
import javasabr.mqtt.acl.engine.model.rule.AllowPublishAclRule

class AllowPublishAclRuleBuilder extends PublishAclRuleBuilder {

  AllowPublishAclRuleBuilder() { super(Action.ALLOW) }

  @Override
  AclRule buildImpl() {
    return new AllowPublishAclRule(userCondition, new TopicCondition(topicMatchers))
  }
}
