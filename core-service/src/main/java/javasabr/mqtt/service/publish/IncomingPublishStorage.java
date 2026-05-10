package javasabr.mqtt.service.publish;

import java.time.Duration;
import java.util.UUID;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.service.publish.exception.AlreadyScheduledForRemovalPublishStorageException;
import javasabr.mqtt.service.publish.exception.NotScheduledForRemovalPublishStorageException;
import javasabr.mqtt.service.publish.exception.UnknownPublishStorageException;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public interface IncomingPublishStorage {

  /**
   * @throws IllegalArgumentException for duplicated publish
   */
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

  /**
   * @throws UnknownPublishStorageException for unknown publish
   */
  void remove(IncomingPublish publish);
  
  void removeIfExist(IncomingPublish publish);

  /**
   * @throws AlreadyScheduledForRemovalPublishStorageException when this publish is already scheduled for removal
   * @throws UnknownPublishStorageException for unknown publish
   */
  void scheduleRemoval(IncomingPublish publish, Duration delay);

  /**
   * @throws NotScheduledForRemovalPublishStorageException when this publish has no scheduled removal entry.
   */
  void cancelScheduledRemoval(IncomingPublish publish);

  void cancelScheduledRemovalIfScheduled(IncomingPublish publish);
  
  /**
   * @throws UnknownPublishStorageException for unknown publish
   */
  void increaseConsumerCount(IncomingPublish publish, int count);
  
  /**
   * @throws UnknownPublishStorageException for unknown publish
   */
  void decreaseConsumerCount(IncomingPublish publish, int count);
}
