package javasabr.mqtt.model.publishing;

import javasabr.mqtt.model.HasMessageId;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;

public record Publish(
    int messageId,
    QoS qos,
    TopicName topicName,
    TopicName responseTopicName,
    byte[] payload,
    boolean duplicated,
    boolean retained,
    String contentType,
    IntArray subscriptionIds,
    byte[] correlationData,
    long messageExpiryInterval,
    int topicAlias,
    PayloadFormat payloadFormat,
    Array<StringPair> userProperties) implements HasMessageId {

  public Publish withDuplicated() {
    if (duplicated) {
      return this;
    }
    return new Publish(
        messageId,
        qos,
        topicName,
        responseTopicName,
        payload,
        true,
        retained,
        contentType,
        subscriptionIds,
        correlationData,
        messageExpiryInterval,
        topicAlias,
        payloadFormat,
        userProperties);
  }

  public Publish with(int messageId, QoS qos, boolean duplicated, int topicAlias) {
    return new Publish(
        messageId,
        qos,
        topicName,
        responseTopicName,
        payload,
        duplicated,
        retained,
        contentType,
        subscriptionIds,
        correlationData,
        messageExpiryInterval,
        topicAlias,
        payloadFormat,
        userProperties);
  }
}
