package javasabr.mqtt.acl.engine.model.rule;

import static javasabr.mqtt.acl.engine.model.Action.DENY;
import static javasabr.mqtt.model.acl.Operation.PUBLISH;

import javasabr.mqtt.acl.engine.model.Action;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.model.acl.Operation;
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
