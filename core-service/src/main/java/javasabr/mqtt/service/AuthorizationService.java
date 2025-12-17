package javasabr.mqtt.service;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;

public interface AuthorizationService {

  boolean authorizePublish(MqttUser user, TopicName topicName);
  
  boolean authorizeSubscribe(MqttUser user, TopicFilter topicFilter);
}
