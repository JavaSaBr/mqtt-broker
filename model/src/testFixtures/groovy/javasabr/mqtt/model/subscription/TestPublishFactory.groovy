package javasabr.mqtt.model.subscription

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.publish.PublishData
import javasabr.mqtt.model.publish.SimpleIncomingPublish
import javasabr.mqtt.model.topic.TopicName
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.IntArray

import static java.nio.charset.StandardCharsets.UTF_8

class TestPublishFactory {

  static def incomingQos0Publish(String topicName) {
    return new SimpleIncomingPublish(
        MqttProperties.MESSAGE_ID_IS_NOT_SET,
        QoS.AT_MOST_ONCE,
        TopicName.valueOf(topicName),
        null,
        PublishData.wrap("payload".getBytes(UTF_8)),
        false,
        false,
        IntArray.empty(),
        60000,
        MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
        Array.empty(StringPair));
  }
  
  static def incomingPublish(QoS qos, String topicName, String payload) {
    return new SimpleIncomingPublish(
        MqttProperties.MESSAGE_ID_IS_NOT_SET,
        qos,
        TopicName.valueOf(topicName),
        null,
        PublishData.wrap(payload.getBytes(UTF_8)),
        false,
        false,
        IntArray.empty(),
        60000,
        MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
        Array.empty(StringPair));
  }

  static def incomingPublish(QoS qos, TopicName topicName, PublishData payload) {
    return new SimpleIncomingPublish(
        MqttProperties.MESSAGE_ID_IS_NOT_SET,
        qos,
        topicName,
        null,
        payload,
        false,
        false,
        IntArray.empty(),
        60000,
        MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
        Array.empty(StringPair));
  }

  static def incomingPublish(int messageId, QoS qos, TopicName topicName, PublishData payload) {
    return new SimpleIncomingPublish(
        messageId,
        qos,
        topicName,
        null,
        payload,
        false,
        false,
        IntArray.empty(),
        60000,
        MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
        Array.empty(StringPair));
  }
  
  static def incomingPublishWithRetain(String topicName, String payload) {
    return new SimpleIncomingPublish(
        1,
        QoS.AT_MOST_ONCE,
        TopicName.valueOf(topicName),
        null,
        PublishData.wrap(payload.getBytes(UTF_8)),
        false,
        true,
        IntArray.of(30),
        60000,
        1,
        Array.empty(StringPair));
  }
}
