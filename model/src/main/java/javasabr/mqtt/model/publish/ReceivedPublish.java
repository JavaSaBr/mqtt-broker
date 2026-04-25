package javasabr.mqtt.model.publish;

import java.util.UUID;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public record ReceivedPublish(
    int messageId,
    QoS qos,
    TopicName topicName,
    @Nullable
    TopicName responseTopicName,
    PublishData publishData,
    boolean duplicated,
    boolean retained,
    IntArray subscriptionIds,
    long messageExpiryInterval,
    int topicAlias,
    Array<StringPair> userProperties) implements Publish {
  
  @Override
  public UUID dataId() {
    return publishData.id();
  }
}
