package javasabr.mqtt.acl.engine.model.rule;

import static javasabr.mqtt.acl.engine.model.Action.ALLOW;
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE;

import javasabr.mqtt.acl.engine.model.Action;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.model.acl.Operation;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class AllowSubscribeAclRule extends AbstractAclRule {

  public AllowSubscribeAclRule(MqttUserCondition userCondition, TopicCondition topicCondition) {
    super(userCondition, topicCondition);
  }

  @Override
  public Operation operation() {
    return SUBSCRIBE;
  }

  @Override
  public Action action() {
    return ALLOW;
  }
}
