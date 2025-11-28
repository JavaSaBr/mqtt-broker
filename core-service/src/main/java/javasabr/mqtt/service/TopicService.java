package javasabr.mqtt.service;

import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface TopicService {

  TopicFilter createTopicFilter(NetworkMqttUser client, String rawTopicFilter);

  boolean isValidTopicFilter(NetworkMqttUser client, String rawTopicFilter);

  TopicName createTopicName(NetworkMqttUser client, String rawTopicName);
}
