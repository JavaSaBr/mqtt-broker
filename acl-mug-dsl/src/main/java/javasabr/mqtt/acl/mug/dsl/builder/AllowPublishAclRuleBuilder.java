package javasabr.mqtt.acl.mug.dsl.builder;

import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.engine.model.rule.AllowPublishAclRule;

public class AllowPublishAclRuleBuilder extends AclRuleBuilder {
  @Override
  protected AclRule buildImpl() {
    return new AllowPublishAclRule(userCondition, new TopicCondition(topicMatchers));
  }
}
