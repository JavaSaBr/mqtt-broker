package javasabr.mqtt.service;

import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface TopicService {

  TopicFilter createTopicFilter(NetworkMqttUser user, String rawTopicFilter);

  boolean isValidTopicFilter(NetworkMqttUser user, String rawTopicFilter);

  TopicName createTopicName(NetworkMqttUser user, String rawTopicName);
}
