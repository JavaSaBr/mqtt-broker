package javasabr.mqtt.model.publish;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public record IncomingPublish(
    int messageId,
    QoS qos,
    TopicName topicName,
    @Nullable
    TopicName responseTopicName,
    PublishData data,
    boolean duplicated,
    boolean retained,
    IntArray subscriptionIds,
    long messageExpiryInterval,
    int topicAlias,
    Array<StringPair> userProperties) implements Publish {
  
  @Override
  public Publish withDuplicated() {
    if (duplicated()) {
      return this;
    } else {
      return new IncomingPublish(
          messageId,
          qos,
          topicName,
          responseTopicName,
          data,
          true,
          retained,
          subscriptionIds,
          messageExpiryInterval,
          topicAlias,
          userProperties);
    }
  }

  @Override
  public Publish withoutRetained() {
    if (!retained()) {
      return this;
    } else {
      return new IncomingPublish(
          messageId,
          qos,
          topicName,
          responseTopicName,
          data,
          duplicated,
          false,
          subscriptionIds,
          messageExpiryInterval,
          topicAlias,
          userProperties);
    }
  }
}
