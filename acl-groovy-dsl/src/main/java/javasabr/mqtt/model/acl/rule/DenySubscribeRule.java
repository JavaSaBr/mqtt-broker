package javasabr.mqtt.model.acl.rule;

import static javasabr.mqtt.model.acl.Action.DENY;
import static javasabr.mqtt.model.acl.Operation.SUBSCRIBE;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.MqttUserCondition;
import javasabr.mqtt.model.acl.condition.TopicCondition;
import javasabr.mqtt.model.topic.AbstractTopic;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DenySubscribeRule extends AbstractRule {

  public DenySubscribeRule(MqttUserCondition userCondition, TopicCondition topicCondition) {
    super(userCondition, topicCondition);
  }

  @Override
  public Operation operation() {
    return SUBSCRIBE;
  }

  @Override
  public Action action() {
    return DENY;
  }
}
