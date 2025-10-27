package javasabr.mqtt.service;

import javasabr.mqtt.model.topic.TopicFilter;

public interface TopicService {

  TopicFilter createTopicFilter(String rawTopicFilter);

  boolean isShared(TopicFilter topicFilter);

  boolean hasWildcard(TopicFilter topicFilter);

  boolean isInvalid(TopicFilter topicFilter);
}
