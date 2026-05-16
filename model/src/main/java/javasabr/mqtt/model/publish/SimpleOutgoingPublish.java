package javasabr.mqtt.model.publish;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public record SimpleOutgoingPublish(
    IncomingPublish source, 
    boolean retained,
    IntArray subscriptionIds) implements OutgoingPublish {

  @Override
  public int messageId() {
    return MqttProperties.MESSAGE_ID_IS_NOT_SET;
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

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
  public boolean duplicated() {
    return false;
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
    throw new UnsupportedOperationException();
  }

  @Override
  public OutgoingPublish withoutRetained() {
    if (!retained()) {
      return this;
    } else {
      return new SimpleOutgoingPublish(source, false, subscriptionIds);
    }
  }
}
