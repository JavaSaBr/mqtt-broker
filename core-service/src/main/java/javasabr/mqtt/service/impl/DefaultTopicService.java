package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.topic.SharedTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.service.TopicService;
import lombok.CustomLog;

@CustomLog
public class DefaultTopicService implements TopicService {

  @Override
  public TopicFilter createTopicFilter(MqttClient client, String rawTopicFilter) {
    if (SharedTopicFilter.isShared(rawTopicFilter)) {
      return createSharedTopicFilter(client, rawTopicFilter);
    }
    return createStandardTopicFilter(client, rawTopicFilter);
  }

  @Override
  public boolean isValidTopicFilter(MqttClient client, String rawTopicFilter) {
    if (SharedTopicFilter.isShared(rawTopicFilter)) {
      if (!TopicValidator.validateSharedTopicFilter(rawTopicFilter)) {
        log.warning(client.clientId(), rawTopicFilter, "[%s] Invalid shared topic filter:[%s]"::formatted);
        return false;
      }
      return true;
    }
    if (!TopicValidator.validateTopicFilter(rawTopicFilter)) {
      log.warning(client.clientId(), rawTopicFilter, "[%s] Invalid topic filter:[%s]"::formatted);
      return false;
    }
    return true;
  }

  @Override
  public TopicName createTopicName(MqttClient client, String rawTopicName) {
    if (!TopicValidator.validateTopicName(rawTopicName)) {
      log.warning(client.clientId(), rawTopicName, "[%s] Invalid topic name:[%s]"::formatted);
      return TopicName.INVALID_TOPIC_NAME;
    }
    return TopicName.valueOf(rawTopicName);
  }

  private TopicFilter createSharedTopicFilter(MqttClient client, String rawTopicFilter) {
    if (!TopicValidator.validateSharedTopicFilter(rawTopicFilter)) {
      log.warning(client.clientId(), rawTopicFilter, "[%s] Invalid shared topic filter:[%s]"::formatted);
      return TopicFilter.INVALID_TOPIC_FILTER;
    }

    SharedTopicFilter sharedTopicFilter = SharedTopicFilter.valueOf(rawTopicFilter);
    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    if (sharedTopicFilter.levelsCount() > connectionConfig.maxTopicLevels()) {
      log.warning(client.clientId(), rawTopicFilter, "[%s] Too deep shared topic filter:[%s]"::formatted);
      return TopicFilter.INVALID_TOPIC_FILTER;
    }

    return sharedTopicFilter;
  }

  private TopicFilter createStandardTopicFilter(MqttClient client, String rawTopicFilter) {
    if (!TopicValidator.validateTopicFilter(rawTopicFilter)) {
      log.warning(client.clientId(), rawTopicFilter, "[%s] Invalid topic filter:[%s]"::formatted);
      return TopicFilter.INVALID_TOPIC_FILTER;
    }

    TopicFilter topicFilter = TopicFilter.valueOf(rawTopicFilter);
    MqttClientConnectionConfig connectionConfig = client.connectionConfig();
    if (topicFilter.levelsCount() > connectionConfig.maxTopicLevels()) {
      log.warning(client.clientId(), rawTopicFilter, "[%s] Too deep topic filter:[%s]"::formatted);
      return TopicFilter.INVALID_TOPIC_FILTER;
    }

    return topicFilter;
  }
}
