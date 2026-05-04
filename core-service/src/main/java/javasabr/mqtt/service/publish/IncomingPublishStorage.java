package javasabr.mqtt.service.publish;

import java.util.UUID;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public interface IncomingPublishStorage {

  IncomingPublish store(
      UUID publishId,
      int messageId,
      QoS qos,
      TopicName topicName,
      @Nullable TopicName responseTopicName,
      PublishData data,
      boolean duplicate,
      boolean retain,
      IntArray subscriptionIds,
      long messageExpiryInterval,
      int topicAlias,
      Array<StringPair> userProperties);
  
  void remove(IncomingPublish publish);
  
  void increaseConsumerCount(IncomingPublish publish, int count);
  
  void decreaseConsumerCount(IncomingPublish publish, int count);
}
