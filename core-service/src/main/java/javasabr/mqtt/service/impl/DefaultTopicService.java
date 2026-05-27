package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.topic.SharedTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.model.topic.TopicValidator;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.TopicService;
import lombok.CustomLog;

@CustomLog
public class DefaultTopicService implements TopicService {

  @Override
  public TopicFilter createTopicFilter(NetworkMqttUser user, String rawTopicFilter) {
    if (SharedTopicFilter.isShared(rawTopicFilter)) {
      return createSharedTopicFilter(user, rawTopicFilter);
    }
    return createStandardTopicFilter(user, rawTopicFilter);
  }

  @Override
  public boolean isValidTopicFilter(NetworkMqttUser user, String rawTopicFilter) {
    if (SharedTopicFilter.isShared(rawTopicFilter)) {
      if (!TopicValidator.validateSharedTopicFilter(rawTopicFilter)) {
        log.warn(user.clientId(), rawTopicFilter, "[%s] Invalid shared topic filter:[%s]"::formatted);
        return false;
      }
      return true;
    }
    if (!TopicValidator.validateTopicFilter(rawTopicFilter)) {
      log.warn(user.clientId(), rawTopicFilter, "[%s] Invalid topic filter:[%s]"::formatted);
      return false;
    }
    return true;
  }

  @Override
  public TopicName createTopicName(NetworkMqttUser user, String rawTopicName) {
    if (!TopicValidator.validateTopicName(rawTopicName)) {
      log.warn(user.clientId(), rawTopicName, "[%s] Invalid topic name:[%s]"::formatted);
      return TopicName.INVALID_TOPIC_NAME;
    }
    return TopicName.valueOf(rawTopicName);
  }

  private TopicFilter createSharedTopicFilter(NetworkMqttUser user, String rawTopicFilter) {
    if (!TopicValidator.validateSharedTopicFilter(rawTopicFilter)) {
      log.warn(user.clientId(), rawTopicFilter, "[%s] Invalid shared topic filter:[%s]"::formatted);
      return TopicFilter.INVALID_TOPIC_FILTER;
    }

    SharedTopicFilter sharedTopicFilter = SharedTopicFilter.valueOf(rawTopicFilter);
    MqttClientConnectionConfig connectionConfig = user.connectionConfig();
    if (sharedTopicFilter.levelsCount() > connectionConfig.maxTopicLevels()) {
      log.warn(user.clientId(), rawTopicFilter, "[%s] Too deep shared topic filter:[%s]"::formatted);
      return TopicFilter.INVALID_TOPIC_FILTER;
    }

    return sharedTopicFilter;
  }

  private TopicFilter createStandardTopicFilter(NetworkMqttUser user, String rawTopicFilter) {
    if (!TopicValidator.validateTopicFilter(rawTopicFilter)) {
      log.warn(user.clientId(), rawTopicFilter, "[%s] Invalid topic filter:[%s]"::formatted);
      return TopicFilter.INVALID_TOPIC_FILTER;
    }

    TopicFilter topicFilter = TopicFilter.valueOf(rawTopicFilter);
    MqttClientConnectionConfig connectionConfig = user.connectionConfig();
    if (topicFilter.levelsCount() > connectionConfig.maxTopicLevels()) {
      log.warn(user.clientId(), rawTopicFilter, "[%s] Too deep topic filter:[%s]"::formatted);
      return TopicFilter.INVALID_TOPIC_FILTER;
    }

    return topicFilter;
  }
}
