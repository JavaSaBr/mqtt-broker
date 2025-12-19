package javasabr.mqtt.service;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.collections.array.Array;

public interface RetainMessageService {

  void retain(Publish publish);

  Array<Publish> findRetainedMessages(TopicFilter topicFilter);
}
