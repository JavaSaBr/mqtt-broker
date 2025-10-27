package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.util.TopicUtils;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.service.TopicService;
import org.jspecify.annotations.Nullable;

public class DefaultTopicService implements TopicService {

  @Nullable
  @Override
  public TopicFilter createTopicFilter(MqttClient client, String rawTopicFilter) {
    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    return TopicUtils.buildTopicFilter(rawTopicFilter);
  }

  @Override
  public boolean isShared(TopicFilter topicFilter) {
    return false;
  }

  @Override
  public boolean hasWildcard(TopicFilter topicFilter) {
    return false;
  }

  @Override
  public boolean isInvalid(TopicFilter topicFilter) {
    return false;
  }
}
