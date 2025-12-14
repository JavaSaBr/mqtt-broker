package javasabr.mqtt.acl.engine.model.rule;

import javasabr.mqtt.acl.engine.model.condition.MqttUserCondition;
import javasabr.mqtt.acl.engine.model.condition.TopicCondition;
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
@Accessors(fluent = true)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public abstract class AbstractRule implements Rule {

  MqttUserCondition userCondition;
  TopicCondition topicCondition;

  @Override
  public boolean test(MqttUser mqttUser, Operation operation, AbstractTopic topic) {
    return operation() == operation && topicCondition.test(topic) && userCondition.test(mqttUser);
  }
}
