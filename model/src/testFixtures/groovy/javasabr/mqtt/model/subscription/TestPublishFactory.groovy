package javasabr.mqtt.model.subscription

import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publishing.Publish
import javasabr.mqtt.model.topic.TopicName
import javasabr.rlib.collections.array.Array
import javasabr.rlib.collections.array.IntArray

import static java.nio.charset.StandardCharsets.UTF_8

class TestPublishFactory {

  static def createPublish(String topicName) {
    return new Publish(
        1,
        QoS.AT_MOST_ONCE,
        TopicName.valueOf(topicName),
        null,
        "payload".getBytes(UTF_8),
        false,
        true,
        null,
        IntArray.of(30),
        null,
        60000,
        1,
        PayloadFormat.UTF8_STRING,
        Array.of());
  }

  static def createPublishWithRetain(String topicName, String payload) {
    return new Publish(
        1,
        QoS.AT_MOST_ONCE,
        TopicName.valueOf(topicName),
        null,
        payload.getBytes(UTF_8),
        false,
        true,
        null,
        IntArray.of(30),
        null,
        60000,
        1,
        PayloadFormat.UTF8_STRING,
        Array.of());
  }

  static def createPublishWithoutRetain(String topicName, String payload) {
    return new Publish(
        1,
        QoS.AT_MOST_ONCE,
        TopicName.valueOf(topicName),
        null,
        payload.getBytes(UTF_8),
        false,
        false,
        null,
        IntArray.of(30),
        null,
        60000,
        1,
        PayloadFormat.UTF8_STRING,
        Array.of());
  }
}
