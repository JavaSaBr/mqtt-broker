package javasabr.mqtt.service.publish;

import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.collections.array.Array;

public interface RetainPublishService {

  void retain(IncomingPublish publish);

  Array<IncomingPublish> findRetainedPublishes(TopicFilter topicFilter);
}
