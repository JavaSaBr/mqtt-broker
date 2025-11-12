package javasabr.mqtt.network.session;

import javasabr.mqtt.model.topic.TopicName;
import org.jspecify.annotations.Nullable;

public interface TopicNameMapping {

  @Nullable
  TopicName resolve(int topicAlias);
}
