package javasabr.mqtt.service;

import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;

public interface TopicService {

  TopicFilter createTopicFilter(MqttClient client, String rawTopicFilter);

  TopicName createTopicName(MqttClient client, String rawTopicName);
}
