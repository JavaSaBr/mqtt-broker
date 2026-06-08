package javasabr.mqtt.acl.antlr.dsl.builder;

import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.engine.model.rule.DenySubscribeAclRule;

public class DenySubscribeAclRuleBuilder extends AclRuleBuilder {
  @Override
  protected AclRule buildImpl() {
    return new DenySubscribeAclRule(userCondition, new TopicCondition(topicMatchers));
  }
}
