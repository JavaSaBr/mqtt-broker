package javasabr.mqtt.acl.engine.model.rule;

import static javasabr.mqtt.acl.engine.model.Action.ALLOW;
import static javasabr.mqtt.model.acl.Operation.PUBLISH;

import javasabr.mqtt.acl.engine.model.Action;
import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.model.acl.Operation;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class AllowPublishAclRule extends AbstractAclRule {

  public AllowPublishAclRule(MqttUserCondition userCondition, TopicCondition topicCondition) {
    super(userCondition, topicCondition);
  }

  @Override
  public Operation operation() {
    return PUBLISH;
  }

  @Override
  public Action action() {
    return ALLOW;
  }
}
