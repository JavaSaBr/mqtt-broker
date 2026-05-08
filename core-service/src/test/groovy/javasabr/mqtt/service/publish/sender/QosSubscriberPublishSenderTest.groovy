package javasabr.mqtt.service.publish.sender

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publish.IncomingPublish
import javasabr.mqtt.model.publish.PublishData
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.network.message.in.MqttInMessage
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.publish.impl.InMemoryIncomingPublishStorage
import javasabr.rlib.collections.array.IntArray
import javasabr.rlib.logger.api.LoggerLevel
import javasabr.rlib.logger.api.LoggerManager

abstract class QosSubscriberPublishSenderTest extends IntegrationServiceSpecification {
  
  static {
    LoggerManager.enable(InMemoryIncomingPublishStorage, LoggerLevel.DEBUG)
  }

  protected IncomingPublish prepareIncomingPublish(
      int messageId,
      QoS qos,
      TopicName topicName,
      byte[] payload) {
    PublishData data = preparePublishData(payload)
    IncomingPublish incomingPublish = defaultIncomingPublishStorage.store(
        UUID.randomUUID(),
        messageId,
        qos,
        topicName,
        null,
        data,
        false,
        false,
        IntArray.empty(),
        60_000,
        MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
        MqttInMessage.EMPTY_USER_PROPERTIES)
    defaultIncomingPublishStorage.increaseConsumerCount(incomingPublish, 1)
    return incomingPublish
  }

  protected PublishData preparePublishData(byte[] payload) {
    return defaultPublishDataStorage.store(
        UUID.randomUUID(),
        null,
        PayloadFormat.BINARY,
        payload,
        null)
  }
}
