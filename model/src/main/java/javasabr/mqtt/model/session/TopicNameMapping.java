package javasabr.mqtt.model.session;

import javasabr.mqtt.model.topic.TopicName;
import org.jspecify.annotations.Nullable;

public interface TopicNameMapping {

  void update(int topicAlias, TopicName topicName);

  @Nullable
  TopicName resolve(int topicAlias);
}
