package javasabr.mqtt.service;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;

public interface AclService {

  boolean authorizePublish(MqttUser mqttUser, TopicName topicName);
  
  boolean authorizeSubscribe(MqttUser mqttUser, TopicFilter topicFilter);
}
