//file:noinspection unused
package javasabr.mqtt.acl.groovy.dsl.builder

import javasabr.mqtt.acl.engine.model.Action
import javasabr.mqtt.acl.engine.model.condition.TopicCondition
import javasabr.mqtt.acl.engine.model.rule.AclRule
import javasabr.mqtt.acl.engine.model.rule.DenySubscribeAclRule

class DenySubscribeAclRuleBuilder extends SubscribeAclRuleBuilder {

  DenySubscribeAclRuleBuilder() { super(Action.DENY) }

  @Override
  AclRule buildImpl() {
    return new DenySubscribeAclRule(userCondition, new TopicCondition(topicMatchers))
  }
}
