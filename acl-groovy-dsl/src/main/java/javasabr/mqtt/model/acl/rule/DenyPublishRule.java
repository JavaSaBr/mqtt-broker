package javasabr.mqtt.model.acl.rule;

import static javasabr.mqtt.model.acl.Action.DENY;
import static javasabr.mqtt.model.acl.Operation.PUBLISH;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.MqttUserCondition;
import javasabr.mqtt.model.acl.condition.TopicCondition;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DenyPublishRule extends AbstractRule {

  public DenyPublishRule(MqttUserCondition userCondition, TopicCondition topicCondition) {
    super(userCondition, topicCondition);
  }

  @Override
  public Operation operation() {
    return PUBLISH;
  }

  @Override
  public Action action() {
    return DENY;
  }
}
