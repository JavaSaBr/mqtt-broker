package javasabr.mqtt.acl.antlr.dsl.builder;

import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.engine.model.rule.DenyPublishAclRule;

public class DenyPublishAclRuleBuilder extends AclRuleBuilder {
  @Override
  protected AclRule buildImpl() {
    return new DenyPublishAclRule(userCondition, new TopicCondition(topicMatchers));
  }
}
