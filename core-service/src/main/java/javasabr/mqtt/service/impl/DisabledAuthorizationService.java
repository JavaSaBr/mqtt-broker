package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.service.AuthorizationService;

public class DisabledAuthorizationService implements AuthorizationService {
  @Override
  public boolean authorizePublish(MqttUser mqttUser, TopicName topicName) {
    return true;
  }

  @Override
  public boolean authorizeSubscribe(MqttUser mqttUser, TopicFilter topicFilter) {
    return true;
  }
}
