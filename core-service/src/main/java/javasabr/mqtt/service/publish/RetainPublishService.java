package javasabr.mqtt.service.publish;

import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.collections.array.Array;
import org.jspecify.annotations.Nullable;

public interface RetainPublishService {

  /**
   * @return the prev. removed retained message or null.
   */
  @Nullable
  IncomingPublish retain(IncomingPublish publish);

  Array<IncomingPublish> findRetainedPublishes(TopicFilter topicFilter);
}
