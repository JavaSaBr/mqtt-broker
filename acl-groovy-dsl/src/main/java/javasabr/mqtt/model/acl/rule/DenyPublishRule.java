package javasabr.mqtt.model.acl.rule;

import static javasabr.mqtt.model.acl.Action.DENY;
import static javasabr.mqtt.model.acl.Operation.PUBLISH;

import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.MqttUserCondition;
import javasabr.mqtt.model.acl.condition.TopicCondition;

public record DenyPublishRule(MqttUserCondition userCondition, TopicCondition topicCondition) implements Rule {

  @Override
  public Operation operation() {
    return PUBLISH;
  }

  @Override
  public Action action() {
    return DENY;
  }
}
