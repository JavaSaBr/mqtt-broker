package javasabr.mqtt.acl.engine.model.rule;

import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.topic.AbstractTopic;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@EqualsAndHashCode
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public abstract class AbstractAclRule implements AclRule {

  static {
    DebugUtils.registerIncludedFields("userCondition", "topicCondition");
  }
  
  MqttUserCondition userCondition;
  TopicCondition topicCondition;

  @Override
  public boolean test(MqttUser mqttUser, Operation operation, AbstractTopic topic) {
    return operation() == operation && topicCondition.test(topic) && userCondition.test(mqttUser);
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
