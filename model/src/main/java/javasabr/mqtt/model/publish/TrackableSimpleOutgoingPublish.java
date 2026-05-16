package javasabr.mqtt.model.publish;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public record TrackableSimpleOutgoingPublish(
    IncomingPublish source,
    int messageId,
    boolean duplicated,
    boolean retained,
    QoS qos,
    IntArray subscriptionIds) implements OutgoingPublish {
  
  @Override
  public TopicName topicName() {
    return source.topicName();
  }
  
  @Nullable
  @Override
  public TopicName responseTopicName() {
    return source.responseTopicName();
  }

  @Override
  public PublishData data() {
    return source.data();
  }

  @Override
  public long messageExpiryInterval() {
    return source.messageExpiryInterval();
  }

  @Override
  public int topicAlias() {
    return MqttProperties.TOPIC_ALIAS_NOT_SET;
  }

  @Override
  public Array<StringPair> userProperties() {
    return source.userProperties();
  }

  @Override
  public OutgoingPublish withDuplicated() {
    if (duplicated()) {
      return this;
    } else {
      return new TrackableSimpleOutgoingPublish(source, messageId, true, retained, qos, subscriptionIds);
    }
  }

  @Override
  public OutgoingPublish withoutRetained() {
    if (!retained()) {
      return this;
    } else {
      return new TrackableSimpleOutgoingPublish(source, messageId, duplicated, false, qos, subscriptionIds);
    }
  }
}
