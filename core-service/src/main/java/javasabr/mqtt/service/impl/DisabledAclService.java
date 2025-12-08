package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.service.AclService;

public class DisabledAclService implements AclService {
  @Override
  public boolean authorizePublish(MqttUser mqttUser, TopicName topicName) {
    return true;
  }

  @Override
  public boolean authorizeSubscribe(MqttUser mqttUser, TopicFilter topicFilter) {
    return true;
  }
}
