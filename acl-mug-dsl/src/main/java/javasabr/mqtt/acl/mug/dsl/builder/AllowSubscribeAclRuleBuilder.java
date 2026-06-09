package javasabr.mqtt.acl.mug.dsl.builder;

import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.acl.engine.model.rule.AclRule;
import javasabr.mqtt.acl.engine.model.rule.AllowSubscribeAclRule;

public class AllowSubscribeAclRuleBuilder extends AclRuleBuilder {
  @Override
  protected AclRule buildImpl() {
    return new AllowSubscribeAclRule(userCondition, new TopicCondition(topicMatchers));
  }
}
