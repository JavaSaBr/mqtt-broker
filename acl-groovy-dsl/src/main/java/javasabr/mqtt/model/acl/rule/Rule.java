package javasabr.mqtt.model.acl.rule;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.acl.Action;
import javasabr.mqtt.model.acl.Operation;
import javasabr.mqtt.model.acl.condition.MqttUserCondition;
import javasabr.mqtt.model.acl.condition.TopicCondition;

public sealed interface Rule permits AllowPublishRule, AllowSubscribeRule, DenyPublishRule, DenySubscribeRule {

  Operation operation();

  Action action();

  MqttUserCondition clientCondition();

  TopicCondition topicCondition();

  default boolean test(MqttUser mqttUser, Operation operation, String topic) {
    return operation() == operation && topicCondition().test(topic) && clientCondition().test(mqttUser);
  }
}
