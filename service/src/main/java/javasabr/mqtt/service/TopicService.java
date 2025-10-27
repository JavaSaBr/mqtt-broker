package javasabr.mqtt.service;

import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttClient;
import org.jspecify.annotations.Nullable;

public interface TopicService {

  @Nullable
  TopicFilter createTopicFilter(MqttClient client, String rawTopicFilter);

  boolean isShared(TopicFilter topicFilter);

  boolean hasWildcard(TopicFilter topicFilter);

  boolean isInvalid(TopicFilter topicFilter);
}
