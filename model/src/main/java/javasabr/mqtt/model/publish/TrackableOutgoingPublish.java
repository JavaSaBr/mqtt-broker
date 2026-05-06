package javasabr.mqtt.model.publish;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public record TrackableOutgoingPublish(
    Publish incomingPublish,
    int messageId,
    boolean duplicated,
    boolean retained,
    QoS qos,
    IntArray subscriptionIds) implements Publish {
  
  @Override
  public TopicName topicName() {
    return incomingPublish.topicName();
  }
  
  @Nullable
  @Override
  public TopicName responseTopicName() {
    return incomingPublish.responseTopicName();
  }

  @Override
  public PublishData data() {
    return incomingPublish.data();
  }

  @Override
  public long messageExpiryInterval() {
    return incomingPublish.messageExpiryInterval();
  }

  @Override
  public int topicAlias() {
    return MqttProperties.TOPIC_ALIAS_NOT_SET;
  }

  @Override
  public Array<StringPair> userProperties() {
    return incomingPublish.userProperties();
  }

  @Override
  public Publish withDuplicated() {
    if (duplicated()) {
      return this;
    } else {
      return new TrackableOutgoingPublish(incomingPublish, messageId, true, retained, qos, subscriptionIds);
    }
  }

  @Override
  public Publish withoutRetained() {
    if (!retained()) {
      return this;
    } else {
      return new TrackableOutgoingPublish(incomingPublish, messageId, duplicated, false, qos, subscriptionIds);
    }
  }
}
