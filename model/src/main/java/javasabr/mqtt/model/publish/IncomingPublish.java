package javasabr.mqtt.model.publish;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.message.MqttMessage;
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

  public static Publish minimal(int messageId, QoS qos, TopicName topicName, PublishData data) {
    return new IncomingPublish(
        messageId,
        qos,
        topicName,
        null,
        data,
        false,
        false,
        IntArray.EMPTY,
        MqttProperties.MESSAGE_EXPIRY_INTERVAL_IS_NOT_SET,
        MqttProperties.TOPIC_ALIAS_NOT_SET,
        MqttMessage.EMPTY_USER_PROPERTIES);
  }

  public static Publish minimal(QoS qos, TopicName topicName, PublishData data) {
    return minimal(MqttProperties.MESSAGE_ID_IS_NOT_SET, qos, topicName, data);
  }
  
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
