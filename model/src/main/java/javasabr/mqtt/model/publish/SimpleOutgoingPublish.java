package javasabr.mqtt.model.publish;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import org.jspecify.annotations.Nullable;

public record SimpleOutgoingPublish(
    Publish incomingPublish, 
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
  public boolean duplicated() {
    return false;
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
    throw new UnsupportedOperationException();
  }

  @Override
  public Publish withoutRetained() {
    if (!retained()) {
      return this;
    } else {
      return new SimpleOutgoingPublish(incomingPublish, false, subscriptionIds);
    }
  }
}
